!include "MUI2.nsh"

;--------------------------------
; General

;Name and file
!define COMPANY "Casterlabs"
!define NAME "Casterlabs-Caffeinated"
Name "${NAME}"
OutFile "dist/Casterlabs-Caffeinated-Setup.exe"
Unicode True

;Default installation folder
InstallDir "$PROGRAMFILES64\${NAME}"

;Request application privileges for Windows Vista
RequestExecutionLevel admin

;--------------------------------
; Interface Settings

!define MUI_ICON "icon.ico"
!define MUI_HEADERIMAGE
;!define MUI_WELCOMEFINISHPAGE_BITMAP "assets\welcome.bmp"
;!define MUI_HEADERIMAGE_BITMAP "assets\head.bmp"
!define MUI_ABORTWARNING
!define MUI_WELCOMEPAGE_TITLE "${NAME} Setup"

;--------------------------------
;Pages

  ; Installer pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "APP_EULA.txt"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!define MUI_PAGE_CUSTOMFUNCTION_SHOW un.ModifyUnWelcome
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_INSTFILES
  
Var deleteUserData ; You could just store the HWND in $1 etc if you don't want this extra variable

;--------------------------------
; Section - Installer

Function .onInit
  ; Check to see if already installed (old installer).
  ReadRegStr $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" "UninstallString"
  IfFileExists $R0 +1 +3
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" ; Forcibly remove the old version.
  RMDir /r "$INSTDIR"
FunctionEnd

Section "App"
  SectionIn RO
  
  SetOutPath "$INSTDIR"
  File /r "dist\build\windows-x86_64\*.*" 
  
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" \
                 "DisplayName" "${NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" \
                 "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" \
                 "QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" \
                 "EstimatedSize" 610000
SectionEnd

Section "Desktop Shortcut" DeskShort
  IfSilent +2 ; Don't create Desktop shortcut when silent.
  CreateShortCut "$DESKTOP\${NAME}.lnk" "$INSTDIR\Casterlabs-Caffeinated.exe"
SectionEnd

Section "Start Menu Shortcut" StartShort
  IfSilent +4 ; Don't create StartMenu shortcut when silent.
  CreateDirectory "$SMPROGRAMS\${COMPANY}"
  CreateShortCut "$SMPROGRAMS\${COMPANY}\${NAME}.lnk" "$INSTDIR\Casterlabs-Caffeinated.exe"
  CreateShortCut "$SMPROGRAMS\${COMPANY}\Uninstall ${NAME}.lnk" "$INSTDIR\uninstall.exe"
SectionEnd

;--------------------------------
; Section - Uninstaller

Function un.ModifyUnWelcome
  ;${NSD_CreateCheckbox} 120u -20u 50% 20u $(DESC_DeleteUserData)
  ;Pop $deleteUserData
  ;SetCtlColors $deleteUserData "" ${MUI_BGCOLOR}
  ;${NSD_Check} $deleteUserData ; Unchecked by default
FunctionEnd

Function un.RMDirUP
  !define RMDirUP '!insertmacro RMDirUPCall'

  !macro RMDirUPCall _PATH
    push '${_PATH}'
    Call un.RMDirUP
  !macroend

  ; $0 - current folder
  ClearErrors

  Exch $0
  ;DetailPrint "ASDF - $0\.."
  RMDir "$0\.."

  IfErrors Skip
  ${RMDirUP} "$0\.."
  Skip:

  Pop $0
FunctionEnd

Section "Uninstall"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}"

  ;${NSD_GetState} $deleteUserData $0
  ;${If} $0 <> 0
  ;  RMDir /r "$APPDATA\casterlabs-caffeinated\preferences" ; Preferences
  ;  ${RMDirUP} "$APPDATA\casterlabs-caffeinated\preferences"
  ;  RMDir /r "$APPDATA\casterlabs-caffeinated\plugins" ; Plugins
  ;  ${RMDirUP} "$APPDATA\casterlabs-caffeinated\plugins"
  ;${EndIf}
  
  RMDir /r "$APPDATA\casterlabs-caffeinated\app" ; App
  RMDir /r "$APPDATA\casterlabs-caffeinated\ipc" ; IPC
  RMDir /r "$APPDATA\casterlabs-caffeinated\api" ; API
  RMDir /r "$APPDATA\casterlabs-caffeinated\logs" ; Logs
  
  Delete "$INSTDIR\Uninstall.exe"

  RMDir /r "$INSTDIR" ; Updater
  ${RMDirUP} "$INSTDIR"
  
  Delete "$DESKTOP\${NAME}.lnk"
  Delete "$SMPROGRAMS\${COMPANY}\${NAME}.lnk"
SectionEnd


;--------------------------------
; Languages
 
!insertmacro MUI_LANGUAGE "English"

;Language strings
LangString DESC_DeskShort ${LANG_ENGLISH} "Create Shortcut on Desktop."
LangString DESC_StartShort ${LANG_ENGLISH} "Create Shortcut on Desktop."
LangString DESC_DeleteUserData ${LANG_ENGLISH} "Delete user preferences and plugins"

;Assign language strings to sections
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${DeskShort} $(DESC_DeskShort)
  !insertmacro MUI_DESCRIPTION_TEXT ${StartShort} $(DESC_StartShort)
!insertmacro MUI_FUNCTION_DESCRIPTION_END
