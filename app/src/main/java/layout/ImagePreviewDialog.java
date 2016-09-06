package layout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

import br.com.plux.checkinfotografico.ImageItem;
import br.com.plux.checkinfotografico.R;
import br.com.plux.checkinfotografico.Util;

/**
 * Created by gustavonobrega on 22/06/2016.
 */
public class ImagePreviewDialog extends DialogFragment {

    private Bitmap bitmapPreview = null;
    private Boolean resized = false;
    LinearLayout previewContainer;
    String tagId = "";
    ImageView imageView = null;
    ImageItem imageItem = null;
    ViewGroup rootView = null;

    public ImagePreviewDialog() {
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setImageItem(ImageItem imageItem) {
        this.imageItem = imageItem;
        this.setTagId(imageItem.getTagId());
    }

    public ImageItem getImageItem() {
        return this.imageItem;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_image_preview, container);
        previewContainer = (LinearLayout) view.findViewById(R.id.containerPreview);
        this.rootView = container;
        getDialog().setTitle("Preview");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Gera o File a partir do caminho da imagem
        File imgFile = new File(this.getImageItem().getRealFile());
        if( imgFile.exists() ) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageBitmap = Util.resizeImage(getContext(), imageBitmap, 500);

            //Gera um novo ImageView
            ImageView imageViewPreview = new ImageView(getContext());
            imageViewPreview.setImageBitmap(imageBitmap);

            //Adiciona a imagem ao container
            if( previewContainer != null ) {
                previewContainer.addView(imageViewPreview);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View myView = getView();

        //Ajusta as dimensões da imagem
        for( int i = 0; i < previewContainer.getChildCount(); i ++ ) {
            if( previewContainer.getChildAt(i) instanceof ImageView ) {
                ImageView imageView = (ImageView)previewContainer.getChildAt(i);
                System.out.println("AQUIIIIIIIII");
                System.out.println(imageView.getLayoutParams().width + "<<<<<<<<<");
                imageView.getLayoutParams().width = 100;
            }
        }

        myView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (!resized) {
                    //Ajusta as dimensões do Fragment
                    Display display = myView.getDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    double heigth = 200 + size.y * 0.3;
                    double width = size.x * 0.2;
                    previewContainer.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    previewContainer.getLayoutParams().height = (int) heigth;
                    previewContainer.requestLayout();

                    //Ajusta as dimensões da imagem
                    /*for( int i = 0; i < previewContainer.getChildCount(); i ++ ) {
                        if( previewContainer.getChildAt(i) instanceof ImageView ) {
                            ImageView imageView = (ImageView)previewContainer.getChildAt(i);
                            imageView.getLayoutParams().width = 100;
                        }
                    }*/
                }
            }
        });
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }
}
