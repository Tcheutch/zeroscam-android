#!/usr/bin/env python3
import os
import re
import sys

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    
    # Vérifier si le fichier contient encore core_domain
    if 'core_domain' not in content:
        return False
    
    # Remplacer package et imports
    old_package_pattern = r'package com\.zeroscam\.core_domain(\.[a-zA-Z0-9._]*)'
    new_package = r'package com.zeroscam.coredomain\1'
    
    old_import_pattern = r'import com\.zeroscam\.core_domain(\.[a-zA-Z0-9._]*)'
    new_import = r'import com.zeroscam.coredomain\1'
    
    # Appliquer les remplacements
    content = re.sub(old_package_pattern, new_package, content)
    content = re.sub(old_import_pattern, new_import, content)
    
    with open(filepath, 'w') as f:
        f.write(content)
    
    return True

def main():
    kotlin_files = []
    for root, dirs, files in os.walk('.'):
        for file in files:
            if file.endswith('.kt'):
                kotlin_files.append(os.path.join(root, file))
    
    print(f"🔍 Analyse de {len(kotlin_files)} fichiers Kotlin")
    
    fixed_count = 0
    for filepath in kotlin_files:
        if fix_file(filepath):
            print(f"✅ Corrigé: {filepath}")
            fixed_count += 1
    
    print(f"\n🎯 Total corrigé: {fixed_count} fichiers")
    
    # Vérifier les fichiers problématiques restants
    print("\n🔍 Vérification finale...")
    remaining = []
    for filepath in kotlin_files:
        with open(filepath, 'r') as f:
            if 'core_domain' in f.read():
                remaining.append(filepath)
    
    if remaining:
        print("⚠️  Fichiers restants avec core_domain:")
        for f in remaining[:10]:
            print(f"  - {f}")
        if len(remaining) > 10:
            print(f"  ... et {len(remaining) - 10} autres")
    else:
        print("✅ Tous les fichiers sont cohérents")

if __name__ == '__main__':
    main()
