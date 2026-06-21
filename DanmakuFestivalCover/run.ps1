$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$javacCommand = Get-Command javac -ErrorAction SilentlyContinue
$javaCommand = Get-Command java -ErrorAction SilentlyContinue
$javacPath = if ($javacCommand) { $javacCommand.Source } else { $null }
$javaPath = if ($javaCommand) { $javaCommand.Source } else { $null }

if (-not $javacPath -or -not $javaPath) {
    $jdkHome = Get-ChildItem "$env:USERPROFILE\.jdks" -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -First 1

    if ($jdkHome) {
        $candidateJavac = Join-Path $jdkHome.FullName "bin\javac.exe"
        $candidateJava = Join-Path $jdkHome.FullName "bin\java.exe"
        if ((Test-Path $candidateJavac) -and (Test-Path $candidateJava)) {
            $javacPath = $candidateJavac
            $javaPath = $candidateJava
        }
    }
}

if (-not $javacPath -or -not $javaPath) {
    Write-Host "未找到 java/javac。请先安装 JDK，或把 JDK 的 bin 目录加入 PATH。"
    exit 1
}

New-Item -ItemType Directory -Path "out" -Force | Out-Null
& $javacPath -encoding UTF-8 -d "out" "src\DanmakuCover.java"
& $javaPath -cp "out" "DanmakuCover"
