//
//  TunerView.swift
//  MyTune
//
//  Main tuner interface with start/stop controls and note display.
//

import SwiftUI
import UIKit

/// Main tuner view
struct TunerView: View {
    @StateObject private var viewModel: TunerViewModel
    @State private var showingPermissionAlert = false
    @State private var previousTuningState: TuningState = .silent
    
    private let hapticGenerator = UINotificationFeedbackGenerator()
    
    init(tuningRepository: TuningRepositoryProtocol,
         settingsRepository: SettingsRepositoryProtocol) {
        _viewModel = StateObject(wrappedValue: TunerViewModel(
            tuningRepository: tuningRepository,
            settingsRepository: settingsRepository
        ))
    }
    
    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                Spacer()
                
                // Note display with octave
                NoteDisplayView(pitchResult: viewModel.pitchResult)
                    .padding(.top, 20)
                
                // Cent deviation display
                CentDisplayView(pitchResult: viewModel.pitchResult)
                    .padding(.vertical, 8)
                
                // In-tune confirmation animation
                if viewModel.pitchResult?.tuningState == .inTune {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 48))
                        .foregroundColor(.green)
                        .scaleEffect(viewModel.showInTuneConfirmation ? 1.2 : 1.0)
                        .animation(.spring(response: 0.3, dampingFraction: 0.6), value: viewModel.showInTuneConfirmation)
                        .transition(.scale.combined(with: .opacity))
                }
                
                // Tuning bar with marker
                TuningBarView(pitchResult: viewModel.pitchResult)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 16)
                
                // String indicators showing target notes
                StringIndicatorsView(
                    tuning: viewModel.currentTuning,
                    pitchResult: viewModel.pitchResult
                )
                .padding(.horizontal, 16)
                
                Spacer()
                
                // Current tuning indicator
                Text("Tuning: \(viewModel.currentTuning.name)")
                    .font(.headline)
                    .foregroundColor(.secondary)
                
                // Start/Stop button
                Button(action: {
                    Task {
                        await viewModel.toggle()
                    }
                }) {
                    HStack(spacing: 12) {
                        Image(systemName: viewModel.isRunning ? "stop.circle.fill" : "play.circle.fill")
                            .font(.title2)
                        
                        Text(viewModel.isRunning ? "Stop" : "Start")
                            .font(.title3)
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(viewModel.isRunning ? Color.red : Color.appPrimary)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .padding(.horizontal, 32)
                
                // Error message
                if let error = viewModel.errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
                
                Spacer()
            }
            .navigationTitle("Guitar Tuner")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        // TODO: Navigate to settings
                    } label: {
                        Image(systemName: "gearshape")
                    }
                }
            }
            .alert(isPresented: $showingPermissionAlert) {
                Alert(
                    title: Text("Microphone Permission Required"),
                    message: Text("Please enable microphone access in Settings to use the tuner."),
                    primaryButton: .default(Text("Open Settings"), action: openSettings),
                    secondaryButton: .cancel()
                )
            }
            .onChange(of: viewModel.pitchResult?.tuningState) { newState in
                handleTuningStateChange(newState: newState ?? .silent)
            }
            .onAppear {
                hapticGenerator.prepare()
            }
        }
        .onDisappear {
            if viewModel.isRunning {
                viewModel.stop()
            }
        }
    }
    
    // MARK: - Private Methods
    
    private func openSettings() {
        if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(settingsURL)
        }
    }
    
    private func handleTuningStateChange(newState: TuningState) {
        // Only trigger confirmation on transition to in-tune (not continuously)
        if newState == .inTune && previousTuningState != .inTune {
            viewModel.triggerInTuneConfirmation()
            triggerHapticFeedback()
        }
        
        previousTuningState = newState
    }
    
    private func triggerHapticFeedback() {
        hapticGenerator.notificationOccurred(.success)
    }
}

#Preview {
    TunerView(
        tuningRepository: TuningRepository(),
        settingsRepository: SettingsRepository()
    )
}
