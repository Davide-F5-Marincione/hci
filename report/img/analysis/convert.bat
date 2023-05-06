@echo off
color 0a

for /r %%f in (.\*.svg) do (
	inkscape --export-type="pdf" %%~nf.svg
)