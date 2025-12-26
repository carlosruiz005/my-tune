#!/usr/bin/env ruby
require 'xcodeproj'

project_path = 'MyTune.xcodeproj'
puts "🔧 Limpiando completamente #{project_path}..."

project = Xcodeproj::Project.open(project_path)
target = project.targets.first

# Limpiar TODO
target.source_build_phase.files.clear
target.resources_build_phase.files.clear

puts "✅ Build phases limpiados"
puts "📊 Archivos fuente: #{target.source_build_phase.files.count}"
puts "📊 Recursos: #{target.resources_build_phase.files.count}"

project.save
puts "✅ Guardado!"
