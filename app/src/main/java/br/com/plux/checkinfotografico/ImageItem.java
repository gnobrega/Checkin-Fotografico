package br.com.plux.checkinfotografico;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

import layout.Checkin;
import layout.ImagePreviewDialog;

/**
 * Created by gustavonobrega on 21/06/2016.
 */
public class ImageItem extends RelativeLayout {

    Long idDb = null;
    Checkin parentActivity;
    //String uri = "";
    Intent data = null;
    Bitmap bitmap = null;
    String tagId = "";
    Integer campaignId = null;
    String campaignName = "";
    Boolean editable = false;
    String realFile = "";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ImageItem(Context context, Checkin parentActivity) {
        super(context);
        this.parentActivity = parentActivity;

        RelativeLayout.LayoutParams params = (LayoutParams) new RelativeLayout.LayoutParams(300, 200);
        this.setLayoutParams(params);
    }

    public Long getIdDb() {
        return idDb;
    }

    public void setIdDb(Long idDb) {
        this.idDb = idDb;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public void setBitmap(Bitmap bmp) {
        this.bitmap = bmp;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getRealFile() {
        return realFile;
    }

    public void setRealFile(String realFile) {
        this.realFile = realFile;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public String getTagId() {
        return this.tagId;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        addCheck();
        addStar();
        final ImageItem parent = this;

        //Clique
        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View imageView) {
                Boolean editable = getEditable();
                showImgCheck();
                showMenu();
            }
        });
    }

    /**
     * Exibe o menu
     */
    public void showMenu() {
        if( parentActivity.getMenu() != null ) {
            for (int i = 0; i < parentActivity.getMenu().size(); i++) {
                parentActivity.getMenu().getItem(i).setVisible(true);
            }
        }
    }

    /**
     * Exibe a imagem em tamanho real
     */
    public void preview() {
        ImageView imageView = this.getImageView();
        ImagePreviewDialog imagePreview = new ImagePreviewDialog();
        android.support.v4.app.FragmentManager fm = this.parentActivity.getFragmentManager();
        imagePreview.setImageView(imageView);
        imagePreview.setImageItem(this);
        imagePreview.show(fm, "fragment_image_preview");
    }

    public ImageView getImageView() {
        for( int i = 0; i < this.getChildCount(); i ++ ) {
            if( this.getChildAt(i) instanceof ImageView ) {
                return (ImageView)this.getChildAt(i);
            }
        }

        return null;
    }

    /**
     * Adiciona a imagem de Check àcima da foto
     */
    public void addCheck() {
        ImageView check = new ImageView(getContext());
        check.setImageResource(R.mipmap.ic_correct);
        RelativeLayout.LayoutParams params = (LayoutParams) new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        check.setVisibility(View.INVISIBLE);
        check.setLayoutParams(params);

        super.addView(check);
    }

    /**
     * Adiciona a imagem de Check àcima da foto
     */
    public void addStar() {
        ImageView star = new ImageView(getContext());
        star.setImageResource(R.mipmap.ic_star);
        RelativeLayout.LayoutParams params = (LayoutParams) new RelativeLayout.LayoutParams(80, 80);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        star.setVisibility(View.INVISIBLE);
        star.setLayoutParams(params);

        super.addView(star);
    }

    /**
     * Exibe o check sobre a imagem
     */
    public void showImgCheck() {
        View check = this.getChildAt(1);
        int isVisible = check.getVisibility();
        if( isVisible == VISIBLE ) {
            check.setVisibility(INVISIBLE);
            this.setEditable(false);
        } else {
            check.setVisibility(VISIBLE);
            this.setEditable(true);
        }
    }

    /**
     * Exibe a estrela referente à campanha
     */
    public void showImgStar() {
        View check = this.getChildAt(2);
        int isVisible = check.getVisibility();
        if( isVisible != VISIBLE ) {
              check.setVisibility(VISIBLE);
        }
    }

    public void hideImgCheck() {
        //RelativeLayout parent = (RelativeLayout) this.getParent();
        View check = getChildAt(1);
        check.setVisibility(INVISIBLE);
    }

    /**
     * Marca a flag de edição
     */
    public void setEditable(Boolean st) {
        this.editable = st;
    }

    /**
     * Consulta a flag de edição
     */
    public Boolean getEditable() {
        return this.editable;
    }

    /*public void setUri(String uri) {
        this.uri = uri;
    }*/

    public void setData(Intent data) {
        this.data = data;
        /*Bundle extras = data.getExtras();
        Bitmap bmp = extras.getParcelable("data");
        this.setBitmap(bmp);*/
    }

    public void setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
    }

    public Integer getCampaignId() {
        return this.campaignId;
    }

    public void remover(Activity activity) {

        //Recupera as imagens
        GridLayout container = (GridLayout)activity.findViewById(R.id.capture);
        if( container != null ) {
            container.removeView(this);
            this.destroyDrawingCache();
        }

        //Remove o arquivo
        String uriFile = getRealFile();
        File file = new File(uriFile);
        if( file.exists() ) {
            file.delete();
        }

        //Remove do banco
        if( this.getIdDb() != null ) {
            DataBase db = new DataBase(this.getContext());
            db.delete(DataBase.TABLE_PHOTO, this.getIdDb());
        }

        //Remove do array
        SharedPreferences sharedpreferences = activity.getSharedPreferences("location", Context.MODE_PRIVATE);
        Integer locationId = sharedpreferences.getInt("id", 0);
        if( Checkin.listImages.containsKey(locationId) ) {
            if( Checkin.listImages.get(locationId).containsKey(this.tagId) ) {
                Checkin.listImages.get(locationId).remove(this.tagId);
            }
        }
    }
}
