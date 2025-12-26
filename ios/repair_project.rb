#!/usr/bin/env ruby
require 'xcodeproj'

project_path = 'MyTune.xcodeproj'
puts "🔧 Reparando proyecto #{project_path}..."

project = Xcodeproj::Project.open(project_path)
target = project.targets.first
main_group = project.main_group['MyTune']

puts "📦 Target: #{target.name}"

# Limpiar build phases
puts "\n🧹 Limpiando build phases..."
target.source_build_phase.files.clear
target.resources_build_phase.files.clear

# Obtener o crear grupo Resources
resources_group = main_group['Resources']
unless resources_group
  resources_group = main_group.new_group('Resources', 'MyTune/Resources')
end

# Agregar presets.json
presets_file = resources_group.files.find { |f| f.path == 'presets.json' }
unless presets_file
  presets_file = resources_group.new_file('presets.json')
end

# Agregar Assets.xcassets
assets = resources_group.children.find { |c| c.path == 'Assets.xcassets' }
unless assets
  assets = resources_group.new_reference('Assets.xcassets')
  assets.last_known_file_type = 'folder.assetcatalog'
end

# Agregar a resources build phase
target.resources_build_phase.add_file_reference(presets_file)
target.resources_build_phase.add_file_reference(assets)

puts "  ✅ presets.json"
puts "  ✅ Assets.xcassets"

# Re-agregar archivos Swift
puts "\n📁 Agregando archivos fuente..."

swift_files = [
  'MyTuneApp.swift',
  'Models/Tuning.swift',
  'Models/GuitarString.swift',
  'Models/PitchResult.swift',
  'Models/TunerSettings.swift',
  'Services/FFTProcessor.swift',
  'Services/HPSPitchDetector.swift',
  'Services/FrequencyConverter.swift',
  'Services/AudioEngine.swift',
  'Services/IOSAudioProcessor.swift',
  'Views/Components/ThemeProvider.swift',
  'Views/Components/NoteDisplayView.swift',
  'Views/Components/TuningBarView.swift',
  'Views/Components/CentDisplayView.swift',
  'Views/Components/StringIndicatorsView.swift',
  'Views/TunerView.swift',
  'ViewModels/TunerViewModel.swift',
  'Repositories/TuningRepository.swift',
  'Repositories/SettingsRepository.swift'
]

def find_file_ref(group, path)
  parts = path.split('/')
  current = group
  
  parts[0..-2].each do |part|
    current = current.children.find { |c| c.display_name == part }
    return nil unless current
  end
  
  current.files.find { |f| f.path == parts.last }
end

swift_files.each do |file_path|
  file_ref = find_file_ref(main_group, file_path)
  if file_ref
    target.add_file_references([file_ref])
    puts "  ✅ #{file_path}"
  else
    puts "  ⚠️  No encontrado: #{file_path}"
  end
end

puts "\n📊 Archivos fuente: #{target.source_build_phase.files.count}"
puts "📊 Recursos: #{target.resources_build_phase.files.count}"

project.save
puts "\n✅ Proyecto reparado exitosamente!"
