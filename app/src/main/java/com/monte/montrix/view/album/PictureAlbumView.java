package com.monte.montrix.view.album;
import android.widget.ImageView;
import android.widget.TextView;
import com.monte.montrix.model.album.PictureAlbumModel;
/** Renders the active album picture and its associated caption. */
public final class PictureAlbumView {
 /** Picture widget. */ private final ImageView image;
 /** Caption widget. */ private final TextView caption;
 /** Creates the album view. @param image image widget @param caption caption widget */
 public PictureAlbumView(ImageView image,TextView caption){this.image=image;this.caption=caption;}
 /** Renders supplied album state. @param model immutable snapshot */
 public void render(PictureAlbumModel model){PictureAlbumModel.Picture current=model.current();if(current==null){image.setImageDrawable(null);caption.setText("");return;}image.setImageURI(current.uri);caption.setText(current.caption);}
}
