# Gera comed-esg.zip na raiz, sem target/, .env, .git e node_modules
$ErrorActionPreference = 'Stop'

$raiz = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$destino = Join-Path $raiz 'comed-esg.zip'
$excluir = '^(target|\.git|node_modules|logs|\.idea|\.vscode)([\\/]|$)|([\\/])(target|node_modules)([\\/]|$)'

if (Test-Path $destino) { Remove-Item $destino -Force }

Add-Type -AssemblyName System.IO.Compression, System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($destino, 'Create')
$total = 0
try {
    Get-ChildItem -Path $raiz -Recurse -File -Force | ForEach-Object {
        $relativo = $_.FullName.Substring($raiz.Length + 1)
        if ($relativo -match $excluir -or $relativo -eq '.env' -or $relativo -eq 'comed-esg.zip') { return }
        $entrada = 'comed-esg/' + ($relativo -replace '\\', '/')
        [void][System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, $entrada, 'Optimal')
        $total++
    }
} finally {
    $zip.Dispose()
}

$tamanho = [math]::Round((Get-Item $destino).Length / 1KB, 1)
Write-Host "comed-esg.zip gerado com $total arquivos ($tamanho KB): $destino"
