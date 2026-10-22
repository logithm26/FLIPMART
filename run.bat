@echo off
title FlipMart - Java Desktop Store
echo Starting FlipMart...
echo Ensuring source files are compiled...
if not exist "bin" mkdir bin
javac -d bin -sourcepath src src\com\shoppingcart\Main.java
java -cp bin com.shoppingcart.Main
pause
