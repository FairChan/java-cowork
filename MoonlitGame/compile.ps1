$ErrorActionPreference = "Stop"

$javaFxVersion = "21.0.8"
$projectRoot = $PSScriptRoot
$depsDir = Join-Path $projectRoot ".deps"
$sdkDir = Join-Path $depsDir "javafx-sdk-$javaFxVersion"
$sdkLib = Join-Path $sdkDir "lib"
$javaFxControls = Join-Path $sdkLib "javafx.controls.jar"

function Find-JdkTool($toolName) {
    $command = Get-Command $toolName -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $jdkHome = Get-ChildItem "$env:USERPROFILE\.jdks" -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if ($jdkHome) {
        $candidate = Join-Path $jdkHome.FullName "bin\$toolName.exe"
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "$toolName not found. Please install a JDK or add it to PATH."
}

if (-not (Test-Path $javaFxControls)) {
    New-Item -ItemType Directory -Force -Path $depsDir | Out-Null
    $zipPath = Join-Path $depsDir "openjfx-$javaFxVersion-windows-x64.zip"
    $url = "https://download2.gluonhq.com/openjfx/$javaFxVersion/openjfx-$javaFxVersion" + "_windows-x64_bin-sdk.zip"
    Write-Host "Downloading JavaFX SDK $javaFxVersion..."
    Invoke-WebRequest -Uri $url -OutFile $zipPath -UseBasicParsing
    Expand-Archive -Path $zipPath -DestinationPath $depsDir -Force
    Remove-Item $zipPath
}

$outDir = Join-Path $projectRoot "out"
$classesDir = Join-Path $outDir "classes"
if (Test-Path $classesDir) {
    Remove-Item -Recurse -Force $classesDir
}
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

$sourceList = Join-Path $outDir "sources.txt"
$javac = Find-JdkTool "javac"
$javaFxClassPath = @(
    (Join-Path $sdkLib "javafx.base.jar"),
    (Join-Path $sdkLib "javafx.graphics.jar"),
    (Join-Path $sdkLib "javafx.controls.jar"),
    (Join-Path $sdkLib "javafx.media.jar")
) -join ";"

Get-ChildItem -Path (Join-Path $projectRoot "src") -Recurse -Filter "*.java" |
    ForEach-Object { $_.FullName } |
    Set-Content -Path $sourceList -Encoding ASCII

& $javac -cp $javaFxClassPath -encoding UTF-8 -d $classesDir "@$sourceList"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
Write-Host "Compiled application into out/classes"
