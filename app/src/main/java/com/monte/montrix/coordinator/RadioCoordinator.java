package com.monte.montrix.coordinator;
import com.monte.montrix.model.radio.RadioModel;
import com.monte.montrix.service.radio.WindowsRtlSdrProvider;
import com.monte.montrix.service.radio.EmulatorRadioAudioService;
/** Coordinates radio model, SDR state, user intents, and audio lifecycle. */
public final class RadioCoordinator {
 private final RadioModel model;private final WindowsRtlSdrProvider provider;private final EmulatorRadioAudioService audio=new EmulatorRadioAudioService();
 private boolean selected;
 public RadioCoordinator(RadioModel state){model=state;provider=new WindowsRtlSdrProvider((f,ps,rt,status)->{
 String detail=(ps!=null&&!ps.isEmpty()?ps+"  ":"")+(rt!=null&&!rt.isEmpty()?rt:"");if(detail.isEmpty())detail=(status!=null&&!status.isEmpty()?status:"SDR connected · RDS not available");
 model.update(f!=null?f:model.primary,detail);});
 model.setActions(new RadioModel.Actions(){public void seek(boolean up){provider.seek(up);if(selected&&model.playing)audio.retune();}
 public void tune(int khz){provider.tune(khz);if(selected&&model.playing)audio.retune();}
 public void play(boolean enabled){if(enabled&&selected){audio.start();model.setPlaying(true);}else{audio.stop();model.setPlaying(false);}}});}
 public void start(){provider.start();}
 public void select(boolean enabled){selected=enabled;if(enabled){provider.start();audio.start();model.setPlaying(true);}else{audio.stop();model.setPlaying(false);}}
 public void close(){select(false);provider.stop();model.setActions(null);}
}
