#!/bin/bash

# Nezha Agent Download Script
# Downloads the latest binary from https://github.com/nichbar/agent releases
# and extracts it to app/src/main/assets/binaries/

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script configuration
GITHUB_REPO="nichbar/agent"
ASSETS_DIR="app/src/main/assets/binaries"
TEMP_DIR="/tmp/nezha-agent-download"

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
    echo "Usage: $0 <architecture>"
    echo ""
    echo "Available architectures:"
    echo "  arm64      - ARM 64-bit (recommended for Android)"
    echo "  amd64      - x86 64-bit"
    echo "  arm        - ARM 32-bit"
    echo "  386        - x86 32-bit"
    echo "  mips       - MIPS"
    echo "  mipsle     - MIPS Little Endian"
    echo "  riscv64    - RISC-V 64-bit"
    echo "  s390x      - IBM S/390"
    echo ""
    echo "Special commands:"
    echo "  --version  - Show current binary version (if exists)"
    echo "  --help     - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 arm64     # Download ARM64 binary (most Android devices)"
    echo "  $0 amd64     # Download x86_64 binary"
    echo "  $0 --version # Check current binary version"
    echo ""
}

# Function to validate architecture
validate_architecture() {
    local arch="$1"
    case "$arch" in
        arm64|amd64|arm|386|mips|mipsle|riscv64|s390x)
            return 0
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
    local missing_deps=()
    
    if ! command -v curl >/dev/null 2>&1; then
        missing_deps+=("curl")
    fi
    
    if ! command -v jq >/dev/null 2>&1; then
        missing_deps+=("jq")
    fi
    
    if ! command -v unzip >/dev/null 2>&1; then
        missing_deps+=("unzip")
    fi
    
    if [ ${#missing_deps[@]} -gt 0 ]; then
        print_error "Missing required dependencies: ${missing_deps[*]}"
        echo ""
        echo "Please install the missing dependencies:"
        if [[ "$OSTYPE" == "darwin"* ]]; then
            echo "  brew install curl jq unzip"
        elif [[ "$OSTYPE" == "linux"* ]]; then
            echo "  # Ubuntu/Debian:"
            echo "  sudo apt-get install curl jq unzip"
            echo "  # CentOS/RHEL:"
            echo "  sudo yum install curl jq unzip"
        fi
        exit 1
    fi
}

# Function to show current binary version
show_current_version() {
    local binary_path="$ASSETS_DIR/nezha-agent"
    
    if [ ! -f "$binary_path" ]; then
        print_warning "No binary found at: $binary_path"
        print_info "Run './download-agent.sh <architecture>' to download a binary"
        return 1
    fi
    
    local file_size=$(ls -lh "$binary_path" | awk '{print $5}')
    local file_date=$(ls -l "$binary_path" | awk '{print $6, $7, $8}')
    
    print_info "Current binary information:"
    echo "  📍 Location: $binary_path"
    echo "  📏 Size: $file_size"
    echo "  📅 Downloaded: $file_date"
    
    # Try to get version from the binary (if it supports --version)
    if timeout 2s "$binary_path" --version 2>/dev/null | head -1; then
        echo ""
    else
        print_info "Binary version information not available"
    fi
}

# Function to get latest release info and extract tag
get_latest_release_tag() {
    local api_url="https://api.github.com/repos/${GITHUB_REPO}/releases/latest"
    
    curl -s -f "$api_url" | jq -r '.tag_name' 2>/dev/null
}

# Function to get release data
get_release_data() {
    local api_url="https://api.github.com/repos/${GITHUB_REPO}/releases/latest"
    
    curl -s -f "$api_url" 2>/dev/null
}

# Function to find download URL for specific architecture
find_download_url() {
    local release_data="$1"
    local arch="$2"
    local os="linux"  # We're targeting Linux binaries for Android
    
    local asset_name="nezha-agent_${os}_${arch}.zip"
    local download_url
    
    download_url=$(echo "$release_data" | jq -r ".assets[] | select(.name == \"$asset_name\") | .browser_download_url")
    
    if [ "$download_url" = "null" ] || [ -z "$download_url" ]; then
        print_error "Could not find download URL for architecture: $arch"
        print_info "Available assets:"
        echo "$release_data" | jq -r '.assets[].name' | grep "nezha-agent_linux" | sed 's/^/  /'
        exit 1
    fi
    
    echo "$download_url"
}

# Function to download and extract binary
download_and_extract() {
    local download_url="$1"
    local release_tag="$2"
    local arch="$3"
    
    # Create temp and assets directories
    mkdir -p "$TEMP_DIR"
    mkdir -p "$ASSETS_DIR"
    
    local zip_file="$TEMP_DIR/nezha-agent_linux_${arch}.zip"
    
    print_info "Downloading binary from: $download_url"
    
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
    
    # Find the extracted binary (should be named 'nezha-agent')
    local extracted_binary="$TEMP_DIR/nezha-agent"
    
    if [ ! -f "$extracted_binary" ]; then
        print_error "Extracted binary not found: $extracted_binary"
        exit 1
    fi
    
    # Copy to assets directory
    local final_binary="$ASSETS_DIR/nezha-agent"
    
    cp "$extracted_binary" "$final_binary"
    chmod +x "$final_binary"
    
    print_success "Binary extracted to: $final_binary"
    
    # Show binary info
    local file_size=$(ls -lh "$final_binary" | awk '{print $5}')
    print_info "Binary size: $file_size"
    print_info "Release version: $release_tag"
    print_info "Architecture: $arch"
    
    # Cleanup
    rm -rf "$TEMP_DIR"
    
    print_success "Download and extraction completed successfully!"
    print_info "You can now build your Android app with the updated binary."
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
        show_current_version
        exit 0
    fi
    
    # Check if architecture argument is provided
    if [ $# -eq 0 ]; then
        print_error "Architecture argument is required"
        show_usage
        exit 1
    fi
    
    local arch="$1"
    
    # Validate architecture
    validate_architecture "$arch"
    
    # Check for required dependencies
    check_dependencies
    
    # Check if we're in the right directory
    if [ ! -f "build.gradle.kts" ]; then
        print_error "Please run this script from the project root directory"
        exit 1
    fi
    
    print_info "Starting download for architecture: $arch"
    
    print_info "Fetching latest release information..."
    
    # Get latest release tag
    local release_tag
    release_tag=$(get_latest_release_tag)
    
    if [ -z "$release_tag" ] || [ "$release_tag" = "null" ]; then
        print_error "Failed to fetch release information from GitHub API"
        exit 1
    fi
    
    print_info "Latest release: $release_tag"
    
    # Get full release data
    local release_data
    release_data=$(get_release_data)
    
    if [ -z "$release_data" ]; then
        print_error "Failed to fetch release data from GitHub API"
        exit 1
    fi
    
    # Find download URL
    local download_url
    download_url=$(find_download_url "$release_data" "$arch")
    
    # Download and extract
    download_and_extract "$download_url" "$release_tag" "$arch"
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
