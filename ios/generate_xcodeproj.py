#!/usr/bin/env python3
import os
import hashlib

def uid(name):
    return hashlib.md5(name.encode('utf-8')).hexdigest()[:24].upper()

ios_dir = os.path.dirname(os.path.abspath(__file__))
promptpay_dir = os.path.join(ios_dir, "PromptPay")

all_files = []
for root, dirs, files in os.walk(promptpay_dir):
    for f in sorted(files):
        all_files.append(os.path.relpath(os.path.join(root, f), ios_dir))

swift_files = [f for f in all_files if f.endswith(".swift")]
font_files = [f for f in all_files if f.endswith(".ttf")]
plist_files = [f for f in all_files if f.endswith("Info.plist")]

# IDs
proj_id = uid("PromptPayProject")
target_id = uid("PromptPayTarget")
main_group_id = uid("MainGroup")
products_group_id = uid("ProductsGroup")
app_product_id = uid("PromptPayAppProduct")
sources_phase_id = uid("SourcesBuildPhase")
frameworks_phase_id = uid("FrameworksBuildPhase")
resources_phase_id = uid("ResourcesBuildPhase")
proj_conf_list_id = uid("ProjConfigList")
proj_debug_conf_id = uid("ProjDebugConfig")
proj_release_conf_id = uid("ProjReleaseConfig")
target_conf_list_id = uid("TargetConfigList")
target_debug_conf_id = uid("TargetDebugConfig")
target_release_conf_id = uid("TargetReleaseConfig")

pbx = []
pbx.append("// !$*UTF8*$!")
pbx.append("{")
pbx.append("\tarchiveVersion = 1;")
pbx.append("\tclasses = {")
pbx.append("\t};")
pbx.append("\tobjectVersion = 56;")
pbx.append("\tobjects = {")

# PBXBuildFile
pbx.append("\n/* Begin PBXBuildFile section */")
for f in swift_files:
    pbx.append(f"\t\t{uid('BF_' + f)} /* {os.path.basename(f)} in Sources */ = {{isa = PBXBuildFile; fileRef = {uid('FR_' + f)} /* {os.path.basename(f)} */; }};")
for f in font_files:
    pbx.append(f"\t\t{uid('BF_' + f)} /* {os.path.basename(f)} in Resources */ = {{isa = PBXBuildFile; fileRef = {uid('FR_' + f)} /* {os.path.basename(f)} */; }};")
pbx.append("/* End PBXBuildFile section */")

# PBXFileReference
pbx.append("\n/* Begin PBXFileReference section */")
pbx.append(f"\t\t{app_product_id} /* PromptPay.app */ = {{isa = PBXFileReference; explicitFileType = wrapper.application; includeInIndex = 0; path = PromptPay.app; sourceTree = BUILT_PRODUCTS_DIR; }};")
for f in swift_files:
    pbx.append(f"\t\t{uid('FR_' + f)} /* {os.path.basename(f)} */ = {{isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = \"{os.path.basename(f)}\"; sourceTree = \"<group>\"; }};")
for f in font_files:
    pbx.append(f"\t\t{uid('FR_' + f)} /* {os.path.basename(f)} */ = {{isa = PBXFileReference; lastKnownFileType = file; path = \"{os.path.basename(f)}\"; sourceTree = \"<group>\"; }};")
for f in plist_files:
    pbx.append(f"\t\t{uid('FR_' + f)} /* {os.path.basename(f)} */ = {{isa = PBXFileReference; lastKnownFileType = text.plist.xml; path = \"{os.path.basename(f)}\"; sourceTree = \"<group>\"; }};")
pbx.append("/* End PBXFileReference section */")

# PBXFrameworksBuildPhase
pbx.append("\n/* Begin PBXFrameworksBuildPhase section */")
pbx.append(f"\t\t{frameworks_phase_id} /* Frameworks */ = {{")
pbx.append("\t\t\tisa = PBXFrameworksBuildPhase;")
pbx.append("\t\t\tbuildActionMask = 2147483647;")
pbx.append("\t\t\tfiles = (")
pbx.append("\t\t\t);")
pbx.append("\t\t\trunOnlyForDeploymentPostprocessing = 0;")
pbx.append("\t\t};")
pbx.append("/* End PBXFrameworksBuildPhase section */")

# Groups
dirs_list = set()
for f in all_files:
    d = os.path.dirname(f)
    dirs_list.add(d)

sorted_dirs = sorted(dirs_list, key=lambda x: x.count(os.sep), reverse=True)

pbx.append("\n/* Begin PBXGroup section */")
for d in sorted_dirs:
    gid = uid("GRP_" + d)
    children = []
    for f in all_files:
        if os.path.dirname(f) == d:
            children.append(f"{uid('FR_' + f)} /* {os.path.basename(f)} */")
    for sd in dirs_list:
        if os.path.dirname(sd) == d and sd != d:
            children.append(f"{uid('GRP_' + sd)} /* {os.path.basename(sd)} */")
    
    path_str = f"path = \"{os.path.basename(d)}\"; " if d != "PromptPay" else "path = PromptPay; "
    pbx.append(f"\t\t{gid} /* {os.path.basename(d)} */ = {{")
    pbx.append("\t\t\tisa = PBXGroup;")
    pbx.append("\t\t\tchildren = (")
    for c in children:
        pbx.append(f"\t\t\t\t{c},")
    pbx.append("\t\t\t);")
    pbx.append(f"\t\t\t{path_str}sourceTree = \"<group>\";")
    pbx.append("\t\t};")

pbx.append(f"\t\t{products_group_id} /* Products */ = {{")
pbx.append("\t\t\tisa = PBXGroup;")
pbx.append("\t\t\tchildren = (")
pbx.append(f"\t\t\t\t{app_product_id} /* PromptPay.app */,")
pbx.append("\t\t\t);")
pbx.append("\t\t\tname = Products;")
pbx.append("\t\t\tsourceTree = \"<group>\";")
pbx.append("\t\t};")

pbx.append(f"\t\t{main_group_id} = {{")
pbx.append("\t\t\tisa = PBXGroup;")
pbx.append("\t\t\tchildren = (")
pbx.append(f"\t\t\t\t{uid('GRP_PromptPay')} /* PromptPay */,")
pbx.append(f"\t\t\t\t{products_group_id} /* Products */,")
pbx.append("\t\t\t);")
pbx.append("\t\t\tsourceTree = \"<group>\";")
pbx.append("\t\t};")
pbx.append("/* End PBXGroup section */")

# PBXNativeTarget
pbx.append("\n/* Begin PBXNativeTarget section */")
pbx.append(f"\t\t{target_id} /* PromptPay */ = {{")
pbx.append("\t\t\tisa = PBXNativeTarget;")
pbx.append(f"\t\t\tbuildConfigurationList = {target_conf_list_id} /* Build configuration list for PBXNativeTarget \"PromptPay\" */;")
pbx.append("\t\t\tbuildPhases = (")
pbx.append(f"\t\t\t\t{sources_phase_id} /* Sources */,")
pbx.append(f"\t\t\t\t{frameworks_phase_id} /* Frameworks */,")
pbx.append(f"\t\t\t\t{resources_phase_id} /* Resources */,")
pbx.append("\t\t\t);")
pbx.append("\t\t\tbuildRules = (")
pbx.append("\t\t\t);")
pbx.append("\t\t\tdependencies = (")
pbx.append("\t\t\t);")
pbx.append("\t\t\tname = PromptPay;")
pbx.append("\t\t\tproductName = PromptPay;")
pbx.append(f"\t\t\tproductReference = {app_product_id} /* PromptPay.app */;")
pbx.append("\t\t\tproductType = \"com.apple.product-type.application\";")
pbx.append("\t\t};")
pbx.append("/* End PBXNativeTarget section */")

# PBXProject
pbx.append("\n/* Begin PBXProject section */")
pbx.append(f"\t\t{proj_id} /* Project object */ = {{")
pbx.append("\t\t\tisa = PBXProject;")
pbx.append("\t\t\tattributes = {")
pbx.append("\t\t\t\tBuildIndependentTargetsInParallel = 1;")
pbx.append("\t\t\t\tLastUpgradeCheck = 1500;")
pbx.append("\t\t\t};")
pbx.append(f"\t\t\tbuildConfigurationList = {proj_conf_list_id} /* Build configuration list for PBXProject \"PromptPay\" */;")
pbx.append("\t\t\tcompatibilityVersion = \"Xcode 14.0\";")
pbx.append("\t\t\tdevelopmentRegion = en;")
pbx.append("\t\t\thasScannedForEncodings = 0;")
pbx.append("\t\t\tknownRegions = (")
pbx.append("\t\t\t\ten,")
pbx.append("\t\t\t\tBase,")
pbx.append("\t\t\t);")
pbx.append(f"\t\t\tmainGroup = {main_group_id};")
pbx.append(f"\t\t\tproductRefGroup = {products_group_id} /* Products */;")
pbx.append("\t\t\tprojectDirPath = \"\";")
pbx.append("\t\t\tprojectRoot = \"\";")
pbx.append("\t\t\ttargets = (")
pbx.append(f"\t\t\t\t{target_id} /* PromptPay */,")
pbx.append("\t\t\t);")
pbx.append("\t\t};")
pbx.append("/* End PBXProject section */")

# PBXResourcesBuildPhase
pbx.append("\n/* Begin PBXResourcesBuildPhase section */")
pbx.append(f"\t\t{resources_phase_id} /* Resources */ = {{")
pbx.append("\t\t\tisa = PBXResourcesBuildPhase;")
pbx.append("\t\t\tbuildActionMask = 2147483647;")
pbx.append("\t\t\tfiles = (")
for f in font_files:
    pbx.append(f"\t\t\t\t{uid('BF_' + f)} /* {os.path.basename(f)} in Resources */,")
pbx.append("\t\t\t);")
pbx.append("\t\t\trunOnlyForDeploymentPostprocessing = 0;")
pbx.append("\t\t};")
pbx.append("/* End PBXResourcesBuildPhase section */")

# PBXSourcesBuildPhase
pbx.append("\n/* Begin PBXSourcesBuildPhase section */")
pbx.append(f"\t\t{sources_phase_id} /* Sources */ = {{")
pbx.append("\t\t\tisa = PBXSourcesBuildPhase;")
pbx.append("\t\t\tbuildActionMask = 2147483647;")
pbx.append("\t\t\tfiles = (")
for f in swift_files:
    pbx.append(f"\t\t\t\t{uid('BF_' + f)} /* {os.path.basename(f)} in Sources */,")
pbx.append("\t\t\t);")
pbx.append("\t\t\trunOnlyForDeploymentPostprocessing = 0;")
pbx.append("\t\t};")
pbx.append("/* End PBXSourcesBuildPhase section */")

# XCBuildConfiguration
pbx.append("\n/* Begin XCBuildConfiguration section */")
pbx.append(f"\t\t{proj_debug_conf_id} /* Debug */ = {{")
pbx.append("\t\t\tisa = XCBuildConfiguration;")
pbx.append("\t\t\tbuildSettings = {")
pbx.append("\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;")
pbx.append("\t\t\t\tCLANG_ENABLE_MODULES = YES;")
pbx.append("\t\t\t\tCOPY_PHASE_STRIP = NO;")
pbx.append("\t\t\t\tDEBUG_INFORMATION_FORMAT = dwarf;")
pbx.append("\t\t\t\tENABLE_STRICT_OBJC_MSGSEND = YES;")
pbx.append("\t\t\t\tENABLE_TESTABILITY = YES;")
pbx.append("\t\t\t\tGCC_DYNAMIC_NO_PIC = NO;")
pbx.append("\t\t\t\tGCC_OPTIMIZATION_LEVEL = 0;")
pbx.append("\t\t\t\tGCC_PREPROCESSOR_DEFINITIONS = (")
pbx.append("\t\t\t\t\t\"DEBUG=1\",")
pbx.append("\t\t\t\t\t\"$(inherited)\",")
pbx.append("\t\t\t\t);")
pbx.append("\t\t\t\tIPHONEOS_DEPLOYMENT_TARGET = 16.0;")
pbx.append("\t\t\t\tMTL_ENABLE_DEBUG_INFO = INCLUDE_SOURCE;")
pbx.append("\t\t\t\tONLY_ACTIVE_ARCH = YES;")
pbx.append("\t\t\t\tSDKROOT = iphoneos;")
pbx.append("\t\t\t\tSWIFT_ACTIVE_COMPILATION_CONDITIONS = DEBUG;")
pbx.append("\t\t\t\tSWIFT_OPTIMIZATION_LEVEL = \"-Onone\";")
pbx.append("\t\t\t};")
pbx.append("\t\t\tname = Debug;")
pbx.append("\t\t};")

pbx.append(f"\t\t{proj_release_conf_id} /* Release */ = {{")
pbx.append("\t\t\tisa = XCBuildConfiguration;")
pbx.append("\t\t\tbuildSettings = {")
pbx.append("\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;")
pbx.append("\t\t\t\tCLANG_ENABLE_MODULES = YES;")
pbx.append("\t\t\t\tCOPY_PHASE_STRIP = NO;")
pbx.append("\t\t\t\tDEBUG_INFORMATION_FORMAT = \"dwarf-with-dsym\";")
pbx.append("\t\t\t\tENABLE_NS_ASSERTIONS = NO;")
pbx.append("\t\t\t\tENABLE_STRICT_OBJC_MSGSEND = YES;")
pbx.append("\t\t\t\tGCC_OPTIMIZATION_LEVEL = s;")
pbx.append("\t\t\t\tIPHONEOS_DEPLOYMENT_TARGET = 16.0;")
pbx.append("\t\t\t\tMTL_ENABLE_DEBUG_INFO = NO;")
pbx.append("\t\t\t\tSDKROOT = iphoneos;")
pbx.append("\t\t\t\tSWIFT_COMPILATION_MODE = \"wholemodule\";")
pbx.append("\t\t\t\tSWIFT_OPTIMIZATION_LEVEL = \"-O\";")
pbx.append("\t\t\t};")
pbx.append("\t\t\tname = Release;")
pbx.append("\t\t};")

pbx.append(f"\t\t{target_debug_conf_id} /* Debug */ = {{")
pbx.append("\t\t\tisa = XCBuildConfiguration;")
pbx.append("\t\t\tbuildSettings = {")
pbx.append("\t\t\t\tCODE_SIGNING_ALLOWED = NO;")
pbx.append("\t\t\t\tCODE_SIGN_IDENTITY = \"\";")
pbx.append("\t\t\t\tCODE_SIGNING_REQUIRED = NO;")
pbx.append("\t\t\t\tCURRENT_PROJECT_VERSION = 1;")
pbx.append("\t\t\t\tGENERATE_INFOPLIST_KEY_UILaunchScreen_Generation = YES;")
pbx.append("\t\t\t\tINFOPLIST_FILE = PromptPay/Info.plist;")
pbx.append("\t\t\t\tLD_RUNPATH_SEARCH_PATHS = (")
pbx.append("\t\t\t\t\t\"$(inherited)\",")
pbx.append("\t\t\t\t\t\"@executable_path/Frameworks\",")
pbx.append("\t\t\t\t);")
pbx.append("\t\t\t\tMARKETING_VERSION = 1.0;")
pbx.append("\t\t\t\tPRODUCT_BUNDLE_IDENTIFIER = com.promptpay.app;")
pbx.append("\t\t\t\tPRODUCT_NAME = \"$(TARGET_NAME)\";")
pbx.append("\t\t\t\tSWIFT_EMIT_LOC_STRINGS = YES;")
pbx.append("\t\t\t\tSWIFT_VERSION = 5.0;")
pbx.append("\t\t\t\tTARGETED_DEVICE_FAMILY = \"1,2\";")
pbx.append("\t\t\t};")
pbx.append("\t\t\tname = Debug;")
pbx.append("\t\t};")

pbx.append(f"\t\t{target_release_conf_id} /* Release */ = {{")
pbx.append("\t\t\tisa = XCBuildConfiguration;")
pbx.append("\t\t\tbuildSettings = {")
pbx.append("\t\t\t\tCODE_SIGNING_ALLOWED = NO;")
pbx.append("\t\t\t\tCODE_SIGN_IDENTITY = \"\";")
pbx.append("\t\t\t\tCODE_SIGNING_REQUIRED = NO;")
pbx.append("\t\t\t\tCURRENT_PROJECT_VERSION = 1;")
pbx.append("\t\t\t\tGENERATE_INFOPLIST_KEY_UILaunchScreen_Generation = YES;")
pbx.append("\t\t\t\tINFOPLIST_FILE = PromptPay/Info.plist;")
pbx.append("\t\t\t\tLD_RUNPATH_SEARCH_PATHS = (")
pbx.append("\t\t\t\t\t\"$(inherited)\",")
pbx.append("\t\t\t\t\t\"@executable_path/Frameworks\",")
pbx.append("\t\t\t\t);")
pbx.append("\t\t\t\tMARKETING_VERSION = 1.0;")
pbx.append("\t\t\t\tPRODUCT_BUNDLE_IDENTIFIER = com.promptpay.app;")
pbx.append("\t\t\t\tPRODUCT_NAME = \"$(TARGET_NAME)\";")
pbx.append("\t\t\t\tSWIFT_EMIT_LOC_STRINGS = YES;")
pbx.append("\t\t\t\tSWIFT_VERSION = 5.0;")
pbx.append("\t\t\t\tTARGETED_DEVICE_FAMILY = \"1,2\";")
pbx.append("\t\t\t};")
pbx.append("\t\t\tname = Release;")
pbx.append("\t\t};")
pbx.append("/* End XCBuildConfiguration section */")

# XCConfigurationList
pbx.append("\n/* Begin XCConfigurationList section */")
pbx.append(f"\t\t{proj_conf_list_id} /* Build configuration list for PBXProject \"PromptPay\" */ = {{")
pbx.append("\t\t\tisa = XCConfigurationList;")
pbx.append("\t\t\tbuildConfigurations = (")
pbx.append(f"\t\t\t\t{proj_debug_conf_id} /* Debug */,")
pbx.append(f"\t\t\t\t{proj_release_conf_id} /* Release */,")
pbx.append("\t\t\t);")
pbx.append("\t\t\tdefaultConfigurationIsVisible = 0;")
pbx.append("\t\t\tdefaultConfigurationName = Release;")
pbx.append("\t\t};")

pbx.append(f"\t\t{target_conf_list_id} /* Build configuration list for PBXNativeTarget \"PromptPay\" */ = {{")
pbx.append("\t\t\tisa = XCConfigurationList;")
pbx.append("\t\t\tbuildConfigurations = (")
pbx.append(f"\t\t\t\t{target_debug_conf_id} /* Debug */,")
pbx.append(f"\t\t\t\t{target_release_conf_id} /* Release */,")
pbx.append("\t\t\t);")
pbx.append("\t\t\tdefaultConfigurationIsVisible = 0;")
pbx.append("\t\t\tdefaultConfigurationName = Release;")
pbx.append("\t\t};")
pbx.append("/* End XCConfigurationList section */")

pbx.append("\t};")
pbx.append(f"\trootObject = {proj_id} /* Project object */;")
pbx.append("}")

out_dir = os.path.join(ios_dir, "PromptPay.xcodeproj")
os.makedirs(out_dir, exist_ok=True)
with open(os.path.join(out_dir, "project.pbxproj"), "w") as fp:
    fp.write("\n".join(pbx) + "\n")

ws_dir = os.path.join(out_dir, "project.xcworkspace")
os.makedirs(ws_dir, exist_ok=True)
with open(os.path.join(ws_dir, "contents.xcworkspacedata"), "w") as fp:
    fp.write('<?xml version="1.0" encoding="UTF-8"?>\n<Workspace\n   version = "1.0">\n   <FileRef\n      location = "self:">\n   </FileRef>\n</Workspace>\n')

scheme_dir = os.path.join(out_dir, "xcshareddata", "xcschemes")
os.makedirs(scheme_dir, exist_ok=True)
scheme_xml = f'''<?xml version="1.0" encoding="UTF-8"?>
<Scheme
   LastUpgradeVersion = "1500"
   version = "1.3">
   <BuildAction
      parallelizeBuildables = "YES"
      buildImplicitDependencies = "YES">
      <BuildActionEntries>
         <BuildActionEntry
            buildForTesting = "YES"
            buildForRunning = "YES"
            buildForProfiling = "YES"
            buildForArchiving = "YES"
            buildForAnalyzing = "YES">
            <BuildableReference
               BuildableIdentifier = "primary"
               BlueprintIdentifier = "{target_id}"
               BuildableName = "PromptPay.app"
               BlueprintName = "PromptPay"
               ReferencedContainer = "container:PromptPay.xcodeproj">
            </BuildableReference>
         </BuildActionEntry>
      </BuildActionEntries>
   </BuildAction>
   <LaunchAction
      buildConfiguration = "Debug"
      selectedDebuggerIdentifier = "Xcode.DebuggerFoundation.Debugger.LLDB"
      selectedLauncherIdentifier = "Xcode.DebuggerFoundation.Launcher.LLDB"
      launchStyle = "0"
      useCustomWorkingDirectory = "NO"
      ignoresPersistentStateOnLaunch = "NO"
      debugDocumentVersioning = "YES"
      debugServiceExtension = "internal"
      allowLocationSimulation = "YES">
      <BuildableProductRunnable
         runnableDebuggingMode = "0">
         <BuildableReference
            BuildableIdentifier = "primary"
            BlueprintIdentifier = "{target_id}"
            BuildableName = "PromptPay.app"
            BlueprintName = "PromptPay"
            ReferencedContainer = "container:PromptPay.xcodeproj">
         </BuildableReference>
      </BuildableProductRunnable>
   </LaunchAction>
</Scheme>'''
with open(os.path.join(scheme_dir, "PromptPay.xcscheme"), "w") as fp:
    fp.write(scheme_xml)

print("Generated ios/PromptPay.xcodeproj successfully!")
