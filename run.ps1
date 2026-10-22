# PowerShell Launcher for FlipMart Desktop Application
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Launching FlipMart Desktop Store Application    " -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "Compiling FlipMart source files..." -ForegroundColor Gray
$javaFiles = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }
javac -d bin -sourcepath src $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful. Starting FlipMart GUI..." -ForegroundColor Green
    java -cp bin com.shoppingcart.Main
} else {
    Write-Host "Compilation encountered errors." -ForegroundColor Red
}
