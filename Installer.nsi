!include "MUI2.nsh"

;--------------------------------
; General

;Name and file
!define COMPANY "Casterlabs"
!define NAME "Casterlabs Caffeinated"
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
  
; Var deleteUserData ; You could just store the HWND in $1 etc if you don't want this extra variable

;--------------------------------
; Section - Installer

Section "Remove Previous Version" RemovePrev
  SectionIn RO
  
  RMDir /r "$INSTDIR"
SectionEnd

Section "App" AppInst
  SectionIn RO
  
  SetOutPath "$INSTDIR"
  File /r "dist\build\windows-x86_64\*.*" 
  
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" \
                 "DisplayName" "${NAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" \
                 "UninstallString" "$\"$INSTDIR\Uninstall.exe$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" \
                 "QuietUninstallString" "$\"$INSTDIR\Uninstall.exe$\" /S"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANY} ${NAME}" \
                 "EstimatedSize" 610000
SectionEnd

Section "Desktop Shortcut" DeskShort
  IfSilent +2 ; Don't create Desktop shortcut when silent.
  CreateShortCut "$DESKTOP\${NAME}.lnk" "$INSTDIR\Casterlabs-Caffeinated-Updater.exe"
SectionEnd

Section "Start Menu Shortcut" StartShort
  IfSilent +4 ; Don't create StartMenu shortcut when silent.
  CreateDirectory "$SMPROGRAMS\${COMPANY}"
  CreateShortCut "$SMPROGRAMS\${COMPANY}\${NAME}.lnk" "$INSTDIR\Casterlabs-Caffeinated-Updater.exe"
  CreateShortCut "$SMPROGRAMS\${COMPANY}\Uninstall ${NAME}.lnk" "$INSTDIR\Uninstall.exe"
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
LangString DESC_RemovePrev ${LANG_ENGLISH} "Remove the previous version (keeping user data intact)."
LangString DESC_AppInst ${LANG_ENGLISH} "Install the app."
LangString DESC_DeskShort ${LANG_ENGLISH} "Create Shortcut on Desktop."
LangString DESC_StartShort ${LANG_ENGLISH} "Create Start Menu folder."
LangString DESC_DeleteUserData ${LANG_ENGLISH} "Delete user preferences and plugins"

;Assign language strings to sections
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${RemovePrev} $(DESC_RemovePrev)
  !insertmacro MUI_DESCRIPTION_TEXT ${AppInst} $(DESC_AppInst)
  !insertmacro MUI_DESCRIPTION_TEXT ${DeskShort} $(DESC_DeskShort)
  !insertmacro MUI_DESCRIPTION_TEXT ${StartShort} $(DESC_StartShort)
!insertmacro MUI_FUNCTION_DESCRIPTION_END
