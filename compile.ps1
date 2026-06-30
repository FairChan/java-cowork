$ErrorActionPreference = "Stop"

$javaFxVersion = "21.0.8"
$projectRoot = $PSScriptRoot
$depsDir = Join-Path $projectRoot ".deps"
$sdkDir = Join-Path $depsDir "javafx-sdk-$javaFxVersion"
$sdkLib = Join-Path $sdkDir "lib"
$javaFxControls = Join-Path $sdkLib "javafx.controls.jar"

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
$testDir = Join-Path $outDir "test"
if (Test-Path $classesDir) {
    Remove-Item -Recurse -Force $classesDir
}
if (Test-Path $testDir) {
    Remove-Item -Recurse -Force $testDir
}
New-Item -ItemType Directory -Force -Path $classesDir, $testDir | Out-Null

$sourceList = Join-Path $outDir "sources.txt"
$testList = Join-Path $outDir "tests.txt"
Get-ChildItem -Path (Join-Path $projectRoot "src") -Recurse -Filter "*.java" |
    ForEach-Object { $_.FullName } |
    Set-Content -Path $sourceList -Encoding ASCII

javac --module-path $sdkLib --add-modules javafx.controls,javafx.media -encoding UTF-8 -d $classesDir "@$sourceList"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Get-ChildItem -Path (Join-Path $projectRoot "test") -Recurse -Filter "*.java" |
    ForEach-Object { $_.FullName } |
    Set-Content -Path $testList -Encoding ASCII

javac --module-path $sdkLib --add-modules javafx.controls,javafx.media -encoding UTF-8 -cp $classesDir -d $testDir "@$testList"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
Write-Host "Compiled application and tests into out/"
