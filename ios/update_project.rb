#!/usr/bin/env ruby

require 'xcodeproj'

project_path = '/Users/carlosalbertoruizrobles/Desarrollos/my-tune/ios/MyTune.xcodeproj'
project = Xcodeproj::Project.open(project_path)

target = project.targets.first
main_group = project.main_group['MyTune']

# Remove all existing file references except Info.plist
main_group.clear
info_plist = main_group.new_file('Info.plist')

# Add MyTuneApp.swift
app_file = main_group.new_file('MyTuneApp.swift')
target.add_file_references([app_file])

# Create groups
models_group = main_group.new_group('Models')
services_group = main_group.new_group('Services')
views_group = main_group.new_group('Views')
components_group = views_group.new_group('Components')
viewmodels_group = main_group.new_group('ViewModels')
repositories_group = main_group.new_group('Repositories')
resources_group = main_group.new_group('Resources')

# Add Model files
['Tuning.swift', 'GuitarString.swift', 'PitchResult.swift', 'TunerSettings.swift'].each do |file|
  file_ref = models_group.new_file("Models/#{file}")
  target.add_file_references([file_ref])
end

# Add Service files
['FFTProcessor.swift', 'HPSPitchDetector.swift', 'FrequencyConverter.swift', 'AudioEngine.swift', 'IOSAudioProcessor.swift'].each do |file|
  file_ref = services_group.new_file("Services/#{file}")
  target.add_file_references([file_ref])
end

# Add ViewModel files
file_ref = viewmodels_group.new_file('ViewModels/TunerViewModel.swift')
target.add_file_references([file_ref])

# Add Repository files
['TuningRepository.swift', 'SettingsRepository.swift'].each do |file|
  file_ref = repositories_group.new_file("Repositories/#{file}")
  target.add_file_references([file_ref])
end

# Add View files
file_ref = views_group.new_file('Views/TunerView.swift')
target.add_file_references([file_ref])

['ThemeProvider.swift', 'NoteDisplayView.swift', 'TuningBarView.swift', 'CentDisplayView.swift', 'StringIndicatorsView.swift'].each do |file|
  file_ref = components_group.new_file("Views/Components/#{file}")
  target.add_file_references([file_ref])
end

# Add Resources
assets_ref = resources_group.new_reference('Resources/Assets.xcassets')
assets_ref.last_known_file_type = 'folder.assetcatalog'
target.resources_build_phase.add_file_reference(assets_ref)

presets_ref = resources_group.new_file('Resources/presets.json')
target.resources_build_phase.add_file_reference(presets_ref)

project.save

puts "Project updated successfully!"
