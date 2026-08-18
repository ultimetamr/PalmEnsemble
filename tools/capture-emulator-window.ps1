param([Parameter(Mandatory=$true)][string]$Out)
Add-Type @"
using System;
using System.Runtime.InteropServices;
public static class NativeWindowCapture {
  [StructLayout(LayoutKind.Sequential)] public struct RECT { public int Left, Top, Right, Bottom; }
  [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);
}
"@
Add-Type -AssemblyName System.Drawing
$process = Get-Process qemu-system-x86_64 | Where-Object { $_.MainWindowTitle -like 'PICO Emulator*' } | Select-Object -First 1
if (-not $process) { throw 'PICO Emulator window not found' }
$rect = New-Object NativeWindowCapture+RECT
[NativeWindowCapture]::GetWindowRect($process.MainWindowHandle, [ref]$rect) | Out-Null
$width = $rect.Right - $rect.Left
$height = $rect.Bottom - $rect.Top
$bitmap = New-Object System.Drawing.Bitmap $width, $height
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.CopyFromScreen($rect.Left, $rect.Top, 0, 0, $bitmap.Size)
$bitmap.Save($Out, [System.Drawing.Imaging.ImageFormat]::Png)
$graphics.Dispose()
$bitmap.Dispose()
