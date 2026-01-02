$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectDir = Resolve-Path (Join-Path $ScriptDir "..")

$JavaBin = $env:JAVA_BIN
if (-not $JavaBin) { $JavaBin = "java" }

$RunnerJar = $env:RUNNER_JAR
if (-not $RunnerJar) { $RunnerJar = Join-Path $ProjectDir "build\libs\jgitkins-runner-0.0.1.jar" }

$RunnerConfig = $env:RUNNER_CONFIG
if (-not $RunnerConfig) { $RunnerConfig = Join-Path $ProjectDir "application.yml" }

$RunnerPid = $env:RUNNER_PID
if (-not $RunnerPid) { $RunnerPid = Join-Path $ProjectDir ".runner\runner.pid" }

$RunnerLog = $env:RUNNER_LOG
if (-not $RunnerLog) { $RunnerLog = Join-Path $ProjectDir ".runner\runner.log" }

function Get-RunnerPid {
  if (-not (Test-Path $RunnerPid)) { return $null }
  $content = Get-Content $RunnerPid -ErrorAction SilentlyContinue
  if (-not $content) { return $null }
  return [int]$content[0]
}

function Is-Running {
  $pid = Get-RunnerPid
  if (-not $pid) { return $false }
  return Get-Process -Id $pid -ErrorAction SilentlyContinue
}

function Start-Runner {
  if (Is-Running) {
    Write-Output "runner is already running (pid=$(Get-RunnerPid))."
    return
  }
  if (-not (Test-Path $RunnerJar)) {
    Write-Error "runner jar not found: $RunnerJar"
    exit 1
  }
  $null = New-Item -ItemType Directory -Force -Path (Split-Path $RunnerPid)
  $null = New-Item -ItemType Directory -Force -Path (Split-Path $RunnerLog)
  $proc = Start-Process -FilePath $JavaBin `
    -ArgumentList @("-jar", $RunnerJar, "start", "--spring.config.location=$RunnerConfig") `
    -RedirectStandardOutput $RunnerLog `
    -RedirectStandardError $RunnerLog `
    -PassThru
  $proc.Id | Out-File -FilePath $RunnerPid -Encoding ascii -Force
  Write-Output "runner started (pid=$($proc.Id))."
}

function Stop-Runner {
  $pid = Get-RunnerPid
  if (-not $pid) {
    Write-Output "runner is not running."
    if (Test-Path $RunnerPid) { Remove-Item $RunnerPid -Force }
    return
  }
  $proc = Get-Process -Id $pid -ErrorAction SilentlyContinue
  if (-not $proc) {
    Write-Output "runner is not running."
    if (Test-Path $RunnerPid) { Remove-Item $RunnerPid -Force }
    return
  }
  Stop-Process -Id $pid -Force
  if (Test-Path $RunnerPid) { Remove-Item $RunnerPid -Force }
  Write-Output "runner stopped."
}

function Status-Runner {
  if (Is-Running) {
    Write-Output "runner is running (pid=$(Get-RunnerPid))."
    exit 0
  }
  Write-Output "runner is not running."
  exit 1
}

function Restart-Runner {
  Stop-Runner
  Start-Runner
}

function Activate-Runner {
  if (-not (Test-Path $RunnerJar)) {
    Write-Error "runner jar not found: $RunnerJar"
    exit 1
  }
  & $JavaBin -jar $RunnerJar activate $args
}

switch ($args[0]) {
  "start" { Start-Runner }
  "stop" { Stop-Runner }
  "status" { Status-Runner }
  "restart" { Restart-Runner }
  "activate" { Activate-Runner }
  default {
    Write-Output "Usage: runner.ps1 {start|stop|status|restart|activate}"
    Write-Output "Env: JAVA_BIN RUNNER_JAR RUNNER_CONFIG RUNNER_PID RUNNER_LOG"
    exit 2
  }
}
