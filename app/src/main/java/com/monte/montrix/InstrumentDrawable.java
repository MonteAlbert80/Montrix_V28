package com.monte.montrix;
import android.graphics.*;import android.graphics.drawable.Drawable;
public class InstrumentDrawable extends Drawable{
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final float radius;private final boolean active;
 public InstrumentDrawable(float r,boolean a){radius=r;active=a;}
 public void draw(Canvas c){
  Rect b=getBounds(); RectF q=new RectF(b.left+1,b.top+1,b.right-1,b.bottom-1);
  // soft shadow/recess
  p.setShader(null);p.setColor(0x99000000);c.drawRoundRect(new RectF(q.left+2,q.top+4,q.right+2,q.bottom+4),radius,radius,p);
  // satin metal rim
  p.setShader(new LinearGradient(0,q.top,0,q.bottom,new int[]{0xff5b6872,0xff26333d,0xff101820,0xff35434e},null,Shader.TileMode.CLAMP));c.drawRoundRect(q,radius,radius,p);
  q.inset(2.2f,2.2f);
  // smoked glass
  p.setShader(new LinearGradient(0,q.top,0,q.bottom,new int[]{0xff152735,0xff09141d,0xff050a0f},new float[]{0f,.42f,1f},Shader.TileMode.CLAMP));c.drawRoundRect(q,Math.max(2,radius-2),Math.max(2,radius-2),p);
  // top glass reflection
  p.setShader(new LinearGradient(0,q.top,0,q.top+Math.max(16,q.height()*.35f),new int[]{0x334cc9ff,0x08000000},null,Shader.TileMode.CLAMP));
  c.drawRoundRect(new RectF(q.left+6,q.top+4,q.right-6,q.top+Math.max(8,q.height()*.30f)),Math.max(2,radius-5),Math.max(2,radius-5),p);
  // fine inner keyline; cyan reserved for active/focal surfaces
  p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(active?1.8f:0.8f);p.setColor(active?0xff24a9ed:0xff52616d);c.drawRoundRect(q,Math.max(2,radius-2),Math.max(2,radius-2),p);p.setStyle(Paint.Style.FILL);
  if(active){p.setColor(0xaa31b9ff);c.drawRoundRect(new RectF(q.left+18,q.top+3,q.right-18,q.top+5),3,3,p);}
 }
 public void setAlpha(int a){} public void setColorFilter(ColorFilter f){} public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}