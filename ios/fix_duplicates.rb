#!/usr/bin/env ruby
require 'xcodeproj'

project_path = 'MyTune.xcodeproj'
puts "🔧 Limpiando referencias duplicadas en #{project_path}..."

project = Xcodeproj::Project.open(project_path)
target = project.targets.first

puts "📦 Target: #{target.name}"

# Obtener todas las referencias actuales
current_files = target.source_build_phase.files_references
puts "📊 Referencias actuales: #{target.source_build_phase.files.count}"

# Limpiar todas las referencias de compilación
target.source_build_phase.files.clear
puts "🧹 Referencias limpiadas"

# Re-agregar archivos únicamente (sin duplicados)
main_group = project.main_group['MyTune']
added_paths = Set.new

def add_source_files_recursively(group, target, added_paths)
  group.children.each do |child|
    if child.is_a?(Xcodeproj::Project::Object::PBXGroup)
      # Es un grupo, procesar recursivamente
      add_source_files_recursively(child, target, added_paths)
    elsif child.is_a?(Xcodeproj::Project::Object::PBXFileReference)
      # Es un archivo
      if child.path.end_with?('.swift') && !added_paths.include?(child.path)
        target.add_file_references([child])
        added_paths.add(child.path)
        puts "  ✅ #{child.path}"
      end
    end
  end
end

puts "\n📁 Re-agregando archivos fuente..."
add_source_files_recursively(main_group, target, added_paths)

puts "\n📊 Referencias finales: #{target.source_build_phase.files.count}"

# Limpiar recursos duplicados
puts "\n🧹 Limpiando recursos duplicados..."
target.resources_build_phase.files.clear

resources_group = main_group['Resources']
added_resource_paths = Set.new

if resources_group
  resources_group.children.each do |child|
    if child.is_a?(Xcodeproj::Project::Object::PBXFileReference)
      if !added_resource_paths.include?(child.path)
        if child.path == 'presets.json'
          # Solo agregar presets.json una vez
          target.resources_build_phase.add_file_reference(child)
          added_resource_paths.add(child.path)
          puts "  ✅ Resources/#{child.path}"
        end
      end
    elsif child.is_a?(Xcodeproj::Project::Object::PBXFileReference) || 
          child.last_known_file_type == 'folder.assetcatalog'
      if !added_resource_paths.include?(child.path)
        target.resources_build_phase.add_file_reference(child)
        added_resource_paths.add(child.path)
        puts "  ✅ Resources/#{child.path}"
      end
    end
  end
  
  # Asegurar que Assets.xcassets esté incluido
  assets = resources_group.children.find { |c| c.path == 'Assets.xcassets' }
  if assets && !added_resource_paths.include?('Assets.xcassets')
    target.resources_build_phase.add_file_reference(assets)
    puts "  ✅ Resources/Assets.xcassets"
  end
end

puts "\n📊 Recursos finales: #{target.resources_build_phase.files.count}"

# Guardar proyecto
project.save
puts "✅ Proyecto guardado exitosamente!"
puts "\n🎯 Ahora ejecuta: xcodebuild clean -scheme MyTune"
