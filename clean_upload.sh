#!/usr/bin/env bash
set -e

echo "=== GitHub Clean Termux Uploader ==="

# 1. Gather Details
read -p "Enter GitHub Username: " USERNAME
read -p "Enter Repository Name: " REPO
read -s -p "Enter Personal Access Token (PAT): " TOKEN
echo ""

# 2. Fix Android Storage Permission for Git
CURRENT_DIR=$(pwd)
echo "[*] Granting Android file system safety permissions..."
git config --global --add safe.directory "$CURRENT_DIR" || true

# 3. Initialize Repo
if [ ! -d ".git" ]; then
    echo "[*] Initializing repository..."
    git init
fi

# 4. Configure local credentials
git config user.name "$USERNAME"
git config user.email "$USERNAME@users.noreply.github.com"

# 5. Clean up old broken links
echo "[*] Resetting connection links..."
git remote remove origin 2>/dev/null || true

# 6. Build the correct URL structure (FIXED)
REMOTE_URL="https://${USERNAME}:${TOKEN}@github.com/${USERNAME}/${REPO}.git"
echo "[*] Using remote URL: https://${USERNAME}:***@github.com/${USERNAME}/${REPO}.git"
git remote add origin "$REMOTE_URL"

# 7. Stage and Commit
echo "[*] Adding your project files..."
git add .

echo "[*] Preparing files for upload..."
git commit -m "Clean bulk upload from Termux" 2>/dev/null || echo "[*] No new changes to commit."

# 8. Push to GitHub
echo "[*] Renaming branch to main..."
git branch -M main

echo "[*] Uploading files to GitHub now..."
git push -u origin main

echo "=== ✅ Project Uploaded Successfully! ==="
