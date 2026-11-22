package com.monte.montrix.model.album;
import android.net.Uri;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Immutable album state, with captions tied to individual pictures. */
public final class PictureAlbumModel {
 /** Metadata for a single selected image. */ public static final class Picture {
  /** Persistable image location. */ public final Uri uri;
  /** Caption displayed only for this image. */ public final String caption;
  /** Creates picture metadata. @param uri image reference @param caption user-defined caption */
  public Picture(Uri uri,String caption){this.uri=uri;this.caption=caption;}
 }
 /** Pictures in user-selected order. */ public final List<Picture> pictures;
 /** Index of the active picture. */ public final int selectedIndex;
 /** Creates an immutable album snapshot. @param pictures images @param selectedIndex active index */
 public PictureAlbumModel(List<Picture> pictures,int selectedIndex){this.pictures=Collections.unmodifiableList(new ArrayList<>(pictures));this.selectedIndex=selectedIndex;}
 /** Returns active picture, if any. @return picture or null */
 public Picture current(){return selectedIndex>=0&&selectedIndex<pictures.size()?pictures.get(selectedIndex):null;}
}
