#!/bin/bash

# Multi-Agent Download Script
# Downloads the latest binary from either:
# - https://github.com/nichbar/agent (Nezha Agent) - ZIP format
# - https://github.com/nichbar/komari-agent (Komari Agent) - Raw binary format
# and extracts/installs it to app/src/main/jniLibs/

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script configuration
TEMP_DIR="/tmp/agent-download"

# Function to get agent repository
get_agent_repo() {
    local agent="$1"
    case "$agent" in
        nezha)
            echo "nichbar/agent"
            ;;
        komari)
            echo "nichbar/komari-agent"
            ;;
        *)
            echo ""
            ;;
    esac
}

# Function to get agent binary name
get_agent_binary_name() {
    local agent="$1"
    case "$agent" in
        nezha)
            echo "nezha-agent"
            ;;
        komari)
            echo "komari-agent"
            ;;
        *)
            echo ""
            ;;
    esac
}

# Function to validate agent type
validate_agent() {
    local agent="$1"
    case "$agent" in
        nezha|komari)
            return 0
            ;;
        *)
            print_error "Invalid agent: $agent"
            print_info "Supported agents: nezha, komari"
            exit 1
            ;;
    esac
}

# Function to get JNI lib directory based on architecture
get_jni_lib_dir() {
    local arch="$1"
    case "$arch" in
        arm64)
            echo "app/src/main/jniLibs/arm64-v8a"
            ;;
        arm)
            echo "app/src/main/jniLibs/armeabi-v7a"
            ;;
        amd64)
            echo "app/src/main/jniLibs/x86_64"
            ;;
        386)
            echo "app/src/main/jniLibs/x86"
            ;;
        *)
            echo "app/src/main/jniLibs/unsupported"
            ;;
    esac
}

# Function to get agent binary name
get_agent_binary_name() {
    local agent="$1"
    case "$agent" in
        nezha)
            echo "nezha-agent"
            ;;
        komari)
            echo "komari-agent"
            ;;
        *)
            echo ""
            ;;
    esac
}

# Function to get agent repository
get_agent_repo() {
    local agent="$1"
    case "$agent" in
        nezha)
            echo "nichbar/agent"
            ;;
        komari)
            echo "nichbar/komari-agent"
            ;;
        *)
            echo ""
            ;;
    esac
}

# Function to validate agent type
validate_agent() {
    local agent="$1"
    case "$agent" in
        nezha|komari)
            return 0
            ;;
        *)
            print_error "Invalid agent: $agent"
            print_info "Supported agents: nezha, komari"
            exit 1
            ;;
    esac
}

# Function to print colored output
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌  $1${NC}"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 <agent> <architecture>"
    echo ""
    echo "Available agents:"
    echo "  nezha      - nichbar/agent"
    echo "  komari     - nichbar/komari-agent"
    echo ""
    echo "Available architectures:"
    echo "  arm64      - ARM 64-bit → arm64-v8a (recommended for Android)"
    echo "  arm        - ARM 32-bit → armeabi-v7a"
    echo "  amd64      - x86 64-bit → x86_64"
    echo "  386        - x86 32-bit → x86"
    echo ""
    echo "Note: Other architectures (mips, mipsle, riscv64, s390x) are not supported for Android JNI"
    echo ""
    echo "Special commands:"
    echo "  --version [agent] [arch]  - Show current binary version (default: nezha arm64)"
    echo "  --help                    - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 nezha arm64                    # Download Nezha Agent ARM64 binary"
    echo "  $0 komari arm64                   # Download Komari Agent ARM64 binary"
    echo "  $0 nezha arm                      # Download Nezha Agent ARM32 binary"
    echo "  $0 --version nezha arm64          # Check Nezha Agent ARM64 binary version"
    echo "  $0 --version komari               # Check Komari Agent (default ARM64) binary version"
    echo ""
    echo "Output locations:"
    echo "  arm64 → app/src/main/jniLibs/arm64-v8a/lib{agent-name}.so"
    echo "  arm   → app/src/main/jniLibs/armeabi-v7a/lib{agent-name}.so"
    echo "  amd64 → app/src/main/jniLibs/x86_64/lib{agent-name}.so"
    echo "  386   → app/src/main/jniLibs/x86/lib{agent-name}.so"
    echo ""
}

# Function to validate architecture
validate_architecture() {
    local arch="$1"
    case "$arch" in
        arm64|arm|amd64|386)
            return 0
            ;;
        mips|mipsle|riscv64|s390x)
            print_error "Architecture '$arch' is not supported for Android JNI"
            print_info "Supported architectures: arm64, arm, amd64, 386"
            exit 1
            ;;
        *)
            print_error "Invalid architecture: $arch"
            show_usage
            exit 1
            ;;
    esac
}

# Function to check dependencies
check_dependencies() {
    local agent="$1"
    local missing_deps=()
    
    if ! command -v curl >/dev/null 2>&1; then
        missing_deps+=("curl")
    fi
    
    if ! command -v jq >/dev/null 2>&1; then
        missing_deps+=("jq")
    fi
    
    # Only require unzip for Nezha agent (uses zip files)
    if [ "$agent" = "nezha" ] && ! command -v unzip >/dev/null 2>&1; then
        missing_deps+=("unzip")
    fi
    
    if [ ${#missing_deps[@]} -gt 0 ]; then
        print_error "Missing required dependencies: ${missing_deps[*]}"
        echo ""
        echo "Please install the missing dependencies:"
        if [[ "$OSTYPE" == "darwin"* ]]; then
            echo "  brew install ${missing_deps[*]}"
        elif [[ "$OSTYPE" == "linux"* ]]; then
            echo "  # Ubuntu/Debian:"
            echo "  sudo apt-get install ${missing_deps[*]}"
            echo "  # CentOS/RHEL:"
            echo "  sudo yum install ${missing_deps[*]}"
        fi
        exit 1
    fi
}

# Function to show current binary version
show_current_version() {
    local agent="nezha"    # Default to nezha
    local arch="arm64"     # Default to arm64
    
    if [ $# -gt 0 ]; then
        agent="$1"
        if [ $# -gt 1 ]; then
            arch="$2"
        fi
    fi
    
    # Validate agent
    validate_agent "$agent"
    
    local jni_dir=$(get_jni_lib_dir "$arch")
    local binary_name=$(get_agent_binary_name "$agent")
    local binary_path="$jni_dir/lib${binary_name}.so"
    
    if [ ! -f "$binary_path" ]; then
        print_warning "No binary found at: $binary_path"
        print_info "Run './download-agent.sh $agent <architecture>' to download a binary"
        print_info "Available binaries:"
        for check_agent in nezha komari; do
            local check_binary_name=$(get_agent_binary_name "$check_agent")
            for check_arch in arm64 arm amd64 386; do
                local check_dir=$(get_jni_lib_dir "$check_arch")
                local check_path="$check_dir/lib${check_binary_name}.so"
                if [ -f "$check_path" ]; then
                    echo "  ✓ $check_agent ($check_arch): $check_path"
                fi
            done
        done
        return 1
    fi
    
    local file_size=$(ls -lh "$binary_path" | awk '{print $5}')
    local file_date=$(ls -l "$binary_path" | awk '{print $6, $7, $8}')
    
    print_info "Current binary information:"
    echo "  📍 Location: $binary_path"
    echo "  📏 Size: $file_size"
    echo "  📅 Downloaded: $file_date"
    echo "  🤖 Agent: $agent"
    echo "  🏗️  Architecture: $arch"
    
    print_info "Binary is ready for Android JNI usage"
}

# Function to get latest release info and extract tag
get_latest_release_tag() {
    local agent="$1"
    local repo=$(get_agent_repo "$agent")
    local api_url="https://api.github.com/repos/${repo}/releases/latest"
    
    curl -s -f "$api_url" | jq -r '.tag_name' 2>/dev/null
}

# Function to get release data
get_release_data() {
    local agent="$1"
    local repo=$(get_agent_repo "$agent")
    local api_url="https://api.github.com/repos/${repo}/releases/latest"
    
    curl -s -f "$api_url" 2>/dev/null
}

# Function to find download URL for specific architecture
find_download_url() {
    local release_data="$1"
    local agent="$2"
    local arch="$3"
    local os="linux"  # We're targeting Linux binaries for Android
    
    local binary_name=$(get_agent_binary_name "$agent")
    local asset_name
    local download_url
    
    # Different naming patterns for different agents
    case "$agent" in
        nezha)
            # Nezha uses zip format: nezha-agent_linux_arm64.zip
            asset_name="${binary_name}_${os}_${arch}.zip"
            ;;
        komari)
            # Komari uses raw binary format: komari-agent-linux-arm64
            asset_name="${binary_name}-${os}-${arch}"
            ;;
    esac
    
    download_url=$(echo "$release_data" | jq -r ".assets[] | select(.name == \"$asset_name\") | .browser_download_url")
    
    if [ "$download_url" = "null" ] || [ -z "$download_url" ]; then
        print_error "Could not find download URL for agent: $agent, architecture: $arch"
        print_info "Looking for asset: $asset_name"
        print_info "Available assets:"
        echo "$release_data" | jq -r '.assets[].name' | grep "${binary_name}" | sed 's/^/  /'
        exit 1
    fi
    
    echo "$download_url"
}

# Function to download and extract binary
download_and_extract() {
    local download_url="$1"
    local release_tag="$2"
    local agent="$3"
    local arch="$4"
    
    # Get the correct JNI directory for this architecture
    local jni_dir=$(get_jni_lib_dir "$arch")
    local binary_name=$(get_agent_binary_name "$agent")
    
    # Validate architecture is supported
    if [ "$jni_dir" = "app/src/main/jniLibs/unsupported" ]; then
        print_error "Architecture '$arch' is not supported for Android JNI"
        print_info "Supported architectures: arm64 (arm64-v8a), arm (armeabi-v7a), amd64 (x86_64), 386 (x86)"
        exit 1
    fi
    
    # Create temp and JNI directories
    mkdir -p "$TEMP_DIR"
    mkdir -p "$jni_dir"
    
    print_info "Downloading binary from: $download_url"
    
    local final_binary="$jni_dir/lib${binary_name}.so"
    
    case "$agent" in
        nezha)
            # Nezha agent comes as a zip file
            local zip_file="$TEMP_DIR/${binary_name}_linux_${arch}.zip"
            
            if ! curl -L -o "$zip_file" "$download_url"; then
                print_error "Failed to download binary"
                exit 1
            fi
            
            print_success "Download completed"
            
            # Extract the binary
            print_info "Extracting binary..."
            
            if ! unzip -o "$zip_file" -d "$TEMP_DIR"; then
                print_error "Failed to extract binary"
                exit 1
            fi
            
            # Find the extracted binary (should be named after the agent)
            local extracted_binary="$TEMP_DIR/$binary_name"
            
            if [ ! -f "$extracted_binary" ]; then
                print_error "Extracted binary not found: $extracted_binary"
                exit 1
            fi
            
            # Copy to JNI directory with .so extension
            cp "$extracted_binary" "$final_binary"
            ;;
            
        komari)
            # Komari agent comes as a raw binary file
            if ! curl -L -o "$final_binary" "$download_url"; then
                print_error "Failed to download binary"
                exit 1
            fi
            
            print_success "Download completed"
            ;;
    esac
    
    chmod +x "$final_binary"
    
    print_success "Binary installed to: $final_binary"
    
    # Show binary info
    local file_size=$(ls -lh "$final_binary" | awk '{print $5}')
    local jni_arch_name=$(basename "$jni_dir")
    
    print_info "Binary size: $file_size"
    print_info "Release version: $release_tag"
    print_info "Agent: $agent"
    print_info "Architecture: $arch → $jni_arch_name"
    print_info "JNI directory: $jni_dir"
    
    # Cleanup
    rm -rf "$TEMP_DIR"
    
    print_success "Download and installation completed successfully!"
    print_info "The binary is now ready to be loaded as a JNI library in your Android app."
    print_info "You can load it in Java/Kotlin using: System.loadLibrary(\"$binary_name\")"
}

# Main script execution
main() {
    # Check for help flag
    if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
        show_usage
        exit 0
    fi
    
    # Check for version flag
    if [ "$1" = "--version" ] || [ "$1" = "-v" ]; then
        shift  # Remove the --version flag
        show_current_version "$@"  # Pass any remaining arguments (like agent and architecture)
        exit 0
    fi
    
    # Check if agent and architecture arguments are provided
    if [ $# -lt 2 ]; then
        print_error "Both agent and architecture arguments are required"
        show_usage
        exit 1
    fi
    
    local agent="$1"
    local arch="$2"
    
    # Validate agent
    validate_agent "$agent"
    
    # Validate architecture
    validate_architecture "$arch"
    
    # Check for required dependencies
    check_dependencies "$agent"
    
    # Check if we're in the right directory
    if [ ! -f "build.gradle.kts" ]; then
        print_error "Please run this script from the project root directory"
        exit 1
    fi
    
    print_info "Starting download for agent: $agent, architecture: $arch"
    
    print_info "Fetching latest release information..."
    
    # Get latest release tag
    local release_tag
    release_tag=$(get_latest_release_tag "$agent")
    
    if [ -z "$release_tag" ] || [ "$release_tag" = "null" ]; then
        print_error "Failed to fetch release information from GitHub API for agent: $agent"
        exit 1
    fi
    
    print_info "Latest release: $release_tag"
    
    # Get full release data
    local release_data
    release_data=$(get_release_data "$agent")
    
    if [ -z "$release_data" ]; then
        print_error "Failed to fetch release data from GitHub API for agent: $agent"
        exit 1
    fi
    
    # Find download URL
    local download_url
    download_url=$(find_download_url "$release_data" "$agent" "$arch")
    
    # Download and extract
    download_and_extract "$download_url" "$release_tag" "$agent" "$arch"
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
