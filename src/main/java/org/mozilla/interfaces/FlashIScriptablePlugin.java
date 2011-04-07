package org.mozilla.interfaces;

/**
 * An IDL interface for the Flash plugin.
 */
public interface FlashIScriptablePlugin extends nsISupports {

      String FLASHISCRIPTABLEPLUGIN_IID = "{d458fe9c-518c-11d6-84cb-0005029bc257}"; //$NON-NLS-1$

       boolean IsPlaying();
       void Play();
       void StopPlay();
       int TotalFrames();
       int CurrentFrame();
       void GotoFrame(int frameNumber);
       void Rewind();
       void Back();
       void Forward();
       void Pan(int x, int y, int mode);
       int PercentLoaded();
       boolean FrameLoaded(int frameNumber);
       int FlashVersion();
       void Zoom(int percent);
       void SetZoomRect(int left, int top, int right, int bottom);
       void LoadMovie(int layerNumber, String url);
       void TGotoFrame(String target, int frameNumber);
       void TGotoLabel(String target, String label);
       int TCurrentFrame(String target);
       String TCurrentLabel(String target);
       void TPlay(String target);
       void TStopPlay(String target);
       void SetVariable(String variableName, String value);
       String GetVariable(String varName);
       void TSetProperty(String target, int property, String value);
       String TGetProperty(String target, int property);
       double TGetPropertyAsNumber(String target, int property);
       void TCallLabel(String target, String label);
       void TCallFrame(String target, int frameNumber);
       void SetWindow(FlashIObject fo, int x);

}