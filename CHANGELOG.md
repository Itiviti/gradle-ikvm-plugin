# Gradle IKVM plugin ChangeLog

## 2.13
* Write `ikvmc.exe.config` alongside `ikvmc.exe` after IKVM extraction to redirect CLR activation to v4.0, fixing execution on Windows Server 2022 where .NET 3.5 / CLR 2.0 is unavailable

## 2.12
* gradle 9.0 support

## 2.11
* gradle 7.0 support

## 2.10
* gradle 5.0 support
* built by gradle 5.4.1
* minimum requirement of jdk is now 1.8
* remove unnecessary dependencies

## 2.9

### Added
* Support nojni / remap / nostdlib options


## 2.8

### Added
* support for customizing the IKVM task input jar(s)

