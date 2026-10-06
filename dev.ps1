# Sobe os 3 serviços (backend, whatsapp-service, dashboard) com um único

param(
    [string]$Action = "start"
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$LogDir = ".dev-logs"
$PidFile = Join-Path $LogDir "pids.json"
$SecretsFile = ".dev-secrets.json"
$EmpresaSlug = "vidracaria-teste"
$EmpresaEmail = "voce@vidracariateste.com"
$EmpresaSenha = "uma-senha-com-8-ou-mais-caracteres"

function Write-Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Fail($msg) { Write-Host "`nERRO: $msg" -ForegroundColor Red; exit 1 }

function Test-Port([int]$port) {
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $iar = $client.BeginConnect("localhost", $port, $null, $null)
        $ok = $iar.AsyncWaitHandle.WaitOne(300)
        $client.Close()
        return $ok
    } catch {
        return $false
    }
}

function New-RandomHex([int]$bytes) {
    $buf = New-Object byte[] $bytes
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($buf)
    -join ($buf | ForEach-Object { $_.ToString("x2") })
}

if ($Action -eq "stop") {
    if (Test-Path $PidFile) {
        Write-Step "Parando processos deste script"
        $procs = Get-Content $PidFile | ConvertFrom-Json
        foreach ($p in $procs) {
            Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
        }
        Remove-Item $PidFile
    }
    Write-Host "`nParado."
    exit 0
}

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
if (-not (Test-Path $PidFile)) { "[]" | Set-Content $PidFile }
$runningPids = @(Get-Content $PidFile | ConvertFrom-Json)

foreach ($cmd in @("mvn", "node", "npm", "java", "mysql")) {
    if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) {
        if ($cmd -eq "mysql") {
            Fail "mysql (cliente) não encontrado no PATH. Instale o MySQL Server (ex: winget install Oracle.MySQL) e abra um novo terminal."
        }
        Fail "$cmd não encontrado no PATH."
    }
}

$javaFirstLine = (& java --version) | Select-Object -First 1
$javaParts = $javaFirstLine -split '\s+'
if ($javaParts.Length -ge 2 -and $javaParts[1] -match '^(\d+)') {
    $javaMajor = [int]$Matches[1]
    if ($javaMajor -lt 25) {
        Fail "JDK 25 é obrigatório (pom.xml está fixado nessa versão). Você tem Java $javaMajor."
    }
} else {
    Fail "Não consegui ler a versão do Java a partir de 'java --version' (saída: '$javaFirstLine')."
}

if (-not (Test-Path $SecretsFile)) {
    Write-Step "Configuração inicial (só acontece uma vez)"
    $dbPassword = Read-Host "Senha do usuário root do seu MySQL local (fica salva só em $SecretsFile, que não vai pro git)"
    $secrets = [ordered]@{
        DB_PASSWORD            = $dbPassword
        JWT_SECRET             = New-RandomHex 32
        WHATSAPP_GATEWAY_TOKEN = New-RandomHex 24
    }
    $secrets | ConvertTo-Json | Set-Content $SecretsFile
}
$secrets = Get-Content $SecretsFile | ConvertFrom-Json
$env:DB_PASSWORD = $secrets.DB_PASSWORD
$env:JWT_SECRET = $secrets.JWT_SECRET
$env:WHATSAPP_GATEWAY_TOKEN = $secrets.WHATSAPP_GATEWAY_TOKEN
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"
$env:CADASTRO_PUBLICO_HABILITADO = "true"
$env:API_DOCS_HABILITADO = "true"

Write-Step "Garantindo o banco 'vidratx' no MySQL local"
$createDbArgs = @("-u", "root", "-p$($env:DB_PASSWORD)", "-e", "CREATE DATABASE IF NOT EXISTS vidratx;")
& mysql @createDbArgs 2>$null
if ($LASTEXITCODE -ne 0) {
    Fail "Não consegui conectar no MySQL com usuário root e a senha salva em $SecretsFile. Apague esse arquivo e rode de novo para digitar a senha correta."
}

if (Test-Port 8080) {
    Write-Host "Backend já respondendo em :8080, não subi de novo."
} else {
    Write-Step "Subindo backend (mvn spring-boot:run) — log em $LogDir\backend.log"
    $mvnPath = (Get-Command mvn).Source
    $backendProc = Start-Process -FilePath $mvnPath -ArgumentList "spring-boot:run" `
        -WorkingDirectory $PSScriptRoot `
        -RedirectStandardOutput "$LogDir\backend.log" `
        -RedirectStandardError "$LogDir\backend.err.log" `
        -WindowStyle Hidden -PassThru
    $runningPids += [ordered]@{ Name = "backend"; Id = $backendProc.Id }

    Write-Host -NoNewline "Esperando o backend responder em :8080"
    $ok = $false
    for ($i = 0; $i -lt 90; $i++) {
        if (Test-Port 8080) { $ok = $true; break }
        Write-Host -NoNewline "."
        Start-Sleep -Seconds 2
    }
    Write-Host ""
    if (-not $ok) { Fail "Backend não respondeu a tempo. Veja $LogDir\backend.log e $LogDir\backend.err.log" }
}

Write-Step "Garantindo empresa de teste ($EmpresaSlug)"
$body = @{
    razaoSocial   = "Vidraçaria Teste LTDA"
    nomeFantasia  = "Vidraçaria Teste"
    cnpj          = "11222333000181"
    slug          = $EmpresaSlug
    emailEmpresa  = "contato@vidracariateste.com"
    telefone      = "11988887777"
    administrador = @{
        nome  = "Seu Nome"
        email = $EmpresaEmail
        senha = $EmpresaSenha
    }
} | ConvertTo-Json
try {
    Invoke-RestMethod -Method Post -Uri "http://localhost:8080/auth/cadastro" -ContentType "application/json" -Body $body | Out-Null
} catch {
}

if (Test-Port 3333) {
    Write-Host "whatsapp-service já respondendo em :3333, não subi de novo."
} else {
    Write-Step "Subindo whatsapp-service — log em $LogDir\whatsapp.log"
    $waDir = Join-Path $PSScriptRoot "whatsapp-service"
    $envFile = Join-Path $waDir ".env"
    if (-not (Test-Path $envFile)) {
        (Get-Content (Join-Path $waDir ".env.example")) `
            -replace '^GATEWAY_TOKEN=.*', "GATEWAY_TOKEN=$($env:WHATSAPP_GATEWAY_TOKEN)" |
            Set-Content $envFile
    }
    if (-not (Test-Path (Join-Path $waDir "node_modules"))) {
        Start-Process -FilePath (Get-Command npm).Source -ArgumentList "install" -WorkingDirectory $waDir -Wait -WindowStyle Hidden
    }
    $waProc = Start-Process -FilePath (Get-Command npm).Source -ArgumentList "run", "dev" `
        -WorkingDirectory $waDir `
        -RedirectStandardOutput "$LogDir\whatsapp.log" `
        -RedirectStandardError "$LogDir\whatsapp.err.log" `
        -WindowStyle Hidden -PassThru
    $runningPids += [ordered]@{ Name = "whatsapp-service"; Id = $waProc.Id }
}

if (Test-Port 5173) {
    Write-Host "Dashboard já respondendo em :5173, não subi de novo."
} else {
    Write-Step "Subindo dashboard — log em $LogDir\dashboard.log"
    $dashDir = Join-Path $PSScriptRoot "dashboard"
    $envLocal = Join-Path $dashDir ".env.local"
    if (-not (Test-Path $envLocal)) {
        Copy-Item (Join-Path $dashDir ".env.example") $envLocal
    }
    if (-not (Test-Path (Join-Path $dashDir "node_modules"))) {
        Start-Process -FilePath (Get-Command npm).Source -ArgumentList "install" -WorkingDirectory $dashDir -Wait -WindowStyle Hidden
    }
    $dashProc = Start-Process -FilePath (Get-Command npm).Source -ArgumentList "run", "dev", "--", "--host" `
        -WorkingDirectory $dashDir `
        -RedirectStandardOutput "$LogDir\dashboard.log" `
        -RedirectStandardError "$LogDir\dashboard.err.log" `
        -WindowStyle Hidden -PassThru
    $runningPids += [ordered]@{ Name = "dashboard"; Id = $dashProc.Id }

    Write-Host -NoNewline "Esperando o dashboard responder em :5173"
    for ($i = 0; $i -lt 60; $i++) {
        if (Test-Port 5173) { break }
        Write-Host -NoNewline "."
        Start-Sleep -Seconds 1
    }
    Write-Host ""
}

$runningPids | ConvertTo-Json | Set-Content $PidFile

Write-Step "Pronto"
Write-Host @"

Abra:      http://localhost:5173/entrar
Empresa:   $EmpresaSlug
E-mail:    $EmpresaEmail
Senha:     $EmpresaSenha

Logs:       $LogDir\backend.log, $LogDir\whatsapp.log, $LogDir\dashboard.log
Para parar: powershell -ExecutionPolicy Bypass -File .\dev.ps1 stop
"@
