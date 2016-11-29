package layout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import br.com.plux.checkinfotografico.App;
import br.com.plux.checkinfotografico.DataBase;
import br.com.plux.checkinfotografico.ImageItem;
import br.com.plux.checkinfotografico.MainActivity;
import br.com.plux.checkinfotografico.R;
import br.com.plux.checkinfotografico.Util;
import br.com.plux.checkinfotografico.bean.PhotoBean;

public class Checkin extends Fragment {
    private Bitmap bitmap;
    private ViewGroup rootView;
    public HashMap<String, Intent> listImage = new HashMap<String, Intent>();
    public static int countImg = 0;
    public static HashMap<Integer, HashMap> listImages = new HashMap<Integer, HashMap>();
    FrameLayout checkinContainer;
    Menu menu = null;
    public Context context = null;

    public static Fragment newInstance(Context context) {
        Checkin c = new Checkin();
        c.context = context;
        return c;
    }

    public static void addImageList(ImageItem imageItem, Integer locationId) {

        //Adiciona à nova campanha
        if( !listImages.containsKey(locationId) ) {
            HashMap<String, ImageItem> capaignItem = new HashMap<>();
            listImages.put(locationId, capaignItem);
        }
        listImages.get(locationId).put(imageItem.getTagId(), imageItem);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //Carrega os dados das imagens armazenadas no banco
        this.loadPhotoDb(getContext(), this);
    }

    public static void loadPhotoDb(Context context, Checkin activity) {

        //Limpa a lista
        Checkin.listImages.clear();

        DataBase db = new DataBase(context);
        List<PhotoBean> lstPhotos = db.loadPhotos();
        for( int i = 0; i < lstPhotos.size(); i++ ) {
            PhotoBean photoBean = lstPhotos.get(i);
            File file = new File(photoBean.getFile());

            //Verifica se o arquivo existe
            if( file.exists() ) {
                countImg++;
                String tagId = "image" + countImg;
                Integer locationId = lstPhotos.get(i).getId_location();
                Bitmap bitmap = Util.getBitmapFile(photoBean.getFile());

                //Gera a imagem
                ImageView novaImagem = new ImageView(context);
                novaImagem.setImageBitmap(Util.resizeImage(context, bitmap, 100));
                novaImagem.setPadding(5,5,5,5);
                novaImagem.setTag(tagId);
                novaImagem.setLongClickable(true);

                //Monta o objeto ImageItem
                ImageItem imageItem = new ImageItem(context, activity);
                imageItem.setIdDb(photoBean.getId());
                imageItem.setTagId(tagId);
                imageItem.addView(novaImagem);
                imageItem.setRealFile(photoBean.getFile());
                Intent intent = new Intent(context, Checkin.class);
                intent.putExtra("BitmapImage", bitmap);
                imageItem.setData(intent);
                if (photoBean.getId_campaign() != null && photoBean.getId_campaign() != 0) {
                    imageItem.setCampaignId(photoBean.getId_campaign());
                    imageItem.setCampaignName(photoBean.getCampaign());
                    imageItem.showImgStar();
                }

                bitmap.recycle();
                bitmap = null;

                //Verifica se a lista de imagens do ponto já foi criada
                if (Checkin.listImages.containsKey(locationId)) {
                    Checkin.listImages.get(locationId).put(tagId, imageItem);
                } else {
                    HashMap<String, ImageItem> lstImages = new HashMap<>();
                    lstImages.put(tagId, imageItem);
                    Checkin.listImages.put(locationId, lstImages);
                }
            } else {

                //Se o arquivo não existir removo o registro do banco
                db.delete(db.TABLE_PHOTO, photoBean.getId());
            }
        }
    }

    public Menu getMenu() {
        return this.menu;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        this.menu = menu;

        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        final HashMap<String, ImageItem> listImages = Checkin.getItensSelected(getActivity());

        //Verifica o menu selecionado
        if (id == R.id.action_attr_camp) { //Menu atribuir campanha

            if( listImages.size() == 0 ) {
                Toast.makeText(getActivity(), "Selecione pelo menos uma imagem", Toast.LENGTH_SHORT).show();
            } else {

                CampaignDialog campaignDialog = new CampaignDialog();
                FragmentManager fm = this.getFragmentManager();
                campaignDialog.show(fm, "fragment_checkin");
            }

            return true;
        } else if(id == R.id.action_attr_preview) { //Menu preview
            if( listImages.size() > 1 ) {
                Toast.makeText(getActivity(), "Selecione apenas uma imagem", Toast.LENGTH_SHORT).show();
            } else if( listImages.size() == 0 ) {
                Toast.makeText(getActivity(), "Selecione pelo menos uma imagem", Toast.LENGTH_SHORT).show();
            } else {
                for (String tagId : listImages.keySet()) {
                    ImageItem imageItem = listImages.get(tagId);
                    imageItem.preview();
                    break;
                }
            }
        } else if(id == R.id.action_attr_delete) { //Menu remover
            if( listImages.size() == 0 ) {
                Toast.makeText(getActivity(), "Selecione pelo menos uma imagem", Toast.LENGTH_SHORT).show();
            } else {


                new AlertDialog.Builder(getContext())
                        .setIcon(R.drawable.ic_alert)
                        .setTitle("Atenção")
                        .setMessage("Tem certeza que deseja realizar a exclusão?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //Remove a lista de itens
                                for (String tagId : listImages.keySet()) {
                                    ImageItem imageItem = listImages.get(tagId);
                                    imageItem.remover(getActivity());
                                }
                            }

                        })
                        .setNegativeButton("Não", null)
                        .show();

            }
        } else if(id == R.id.action_attr_ver_capanha) { //Menu Ver Campanha
            if( listImages.size() > 1 ) {
                Toast.makeText(getActivity(), "Selecione apenas uma imagem", Toast.LENGTH_SHORT).show();
            } else if( listImages.size() == 0 ) {
                Toast.makeText(getActivity(), "Selecione pelo menos uma imagem", Toast.LENGTH_SHORT).show();
            } else {
                String msg = null;
                for (String tagId : listImages.keySet()) {
                    ImageItem imageItem = listImages.get(tagId);
                    if( imageItem.getCampaignId() != null ) {
                        msg = "Campanha: " + imageItem.getCampaignName();
                    } else {
                        msg = "Ainda não foi atribuída uma campanha para essa foto";
                    }
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View myView = getView();

        myView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                //Se as dimensões do Fragment
                checkinContainer.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                checkinContainer.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
                checkinContainer.requestLayout();

                //Esconde o teclado
                if (getActivity() != null) {
                    View view = getActivity().getCurrentFocus();
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_checkin, null);
        checkinContainer = (FrameLayout)rootView.findViewById(R.id.checkinContent);

        //Clique no botão da câmera
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                openCam();
            }
        });

        //Limpa a grid
        GridLayout grid = (GridLayout)rootView.findViewById(R.id.capture);
        grid.removeAllViews();

        //Exibe o ponto selecionado
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("location", Context.MODE_PRIVATE);
        Integer locationId = sharedpreferences.getInt("id", 0);
        if( sharedpreferences != null && locationId != 0 ) {
            TextView locationName = (TextView)rootView.findViewById(R.id.checkinLocationName);
            locationName.append(sharedpreferences.getString("name", ""));

            //Devolve as imagens já cadastradas à grid
            if( Checkin.listImages.containsKey(locationId) && Checkin.listImages.get(locationId) != null ) {
                HashMap<String, ImageItem> lstImages = Checkin.listImages.get(locationId);

                //Container
                grid.setColumnCount(2);
                grid.removeAllViews();

                for( String tagId : lstImages.keySet() ) {
                    ImageItem imageItem = (ImageItem)lstImages.get(tagId);
                    if( imageItem.getParent() != null ) {
                        ((GridLayout)imageItem.getParent()).removeView(imageItem);
                    }
                    grid.addView(imageItem);
                }
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Inicia a camera
     */
    Uri mImageUri;
    String urlFile;
    public void openCam() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("android.intent.extra.quickCapture", true);
        String pathPhotos = App.PATH_PHOTOS;

        //Verifica se o aparelho possui a permissão de escrita no sdcard
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Integer PERMISSIONS_REQUEST_WRITE = 1;
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE);
        }

        File dir = new File(pathPhotos);
        if( !dir.exists() ) {
            dir.mkdir();
        }
        try {
            File photo = File.createTempFile("picture", ".jpg", dir);
            photo.delete();
            urlFile = photo.getAbsolutePath();
            mImageUri = Uri.fromFile(photo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Verifica se o aparelho possui a permissão da câmera
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getContext(), "Permissão de acesso à câmera negada",Toast.LENGTH_SHORT);
            toast.show();

            Integer PERMISSIONS_REQUEST_CAMERA = 1;

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);

            startActivityForResult(intent, 0);
        } else {
            startActivityForResult(intent, 0);
        }


    }

    /**
     * Recebe a foto
     */
    public void onActivityResult(int resquestCode, int resultCode, Intent data) {
        super.onActivityResult(resquestCode, resultCode, data);
        if(resquestCode == 0 && resultCode == Activity.RESULT_OK) {
            addImage(data);
            openCam();
        }
    }

    /**
     * Adiciona a imagem ao array
     */
    public void addImage(Intent data) {

        InputStream stream = null;
        if(bitmap != null) {
            bitmap.recycle();
        }
        try {
            countImg ++;
            String tagId = "image"+ countImg;
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageUri);

            //Comprime a imagem
            Util.compressImage(getContext(), urlFile);

            //Adiciona ao banco de dados
            DataBase dataBase = new DataBase(getContext());
            Long photoDbId = dataBase.insertPhoto(urlFile, null, "", Util.getUserSession(getContext()).getId(), getLocation());

            //Adiciona à grid
            //addGridImg(bitmap, tagId, data.getDataString(), data, urlFile, photoDbId);
            addGridImg(bitmap, tagId, data, urlFile, photoDbId);

            //Armazena no array
            listImage.put(tagId, data);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if( stream != null ) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Adiciona a imagem à grid
     */
    public void addGridImg(Bitmap bitmap, String tagId, Intent data, String urlFile, Long photoDbId) {

        //Container
        GridLayout container = (GridLayout)rootView.findViewById(R.id.capture);
        container.setColumnCount(2);

        //Cria um novo item de imagem
        ImageItem imageItem = new ImageItem(getContext(), this);
        final ImageView novaImagem = new ImageView(getContext());
        novaImagem.setImageBitmap(Util.resizeImage(rootView.getContext(), bitmap, 100));
        novaImagem.setPadding(5,5,5, 5);
        novaImagem.setTag(tagId);
        novaImagem.setLongClickable(true);
        //imageItem.setUri(uri);
        imageItem.setTagId(tagId);
        imageItem.setData(data);
        imageItem.addView(novaImagem);
        imageItem.setRealFile(urlFile);
        imageItem.setIdDb(photoDbId);
        container.addView(imageItem);

        bitmap.recycle();
        bitmap = null;

        //Armazena o item na lista global de imagens
        Integer locationId = getLocation();
        Checkin.addImageList(imageItem, locationId);
    }

    public Integer getLocation() {
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("location", Context.MODE_PRIVATE);
        Integer locationId = sharedpreferences.getInt("id", 0);
        return locationId;
    }

    /**
     * Evento chamado ao clicar no botão de volta
     */
    public void onBack(MainActivity activity) {

        //Recupera as imagens
        GridLayout container = (GridLayout)activity.findViewById(R.id.capture);

        if( container != null ) {
            for (int i = 0; i < container.getChildCount(); i++) {
                View item = container.getChildAt(i);
                if (item instanceof ImageItem) {
                    ((ImageItem) item).setEditable(false);
                    ((ImageItem) item).hideImgCheck();
                }
            }
        }
    }

    public static HashMap<String, ImageItem> getItensSelected(Activity activity) {

        //Recupera as imagens
        GridLayout container = (GridLayout)activity.findViewById(R.id.capture);
        HashMap<String, ImageItem> listItens = new HashMap<String, ImageItem>();

        for( int i = 0; i < container.getChildCount(); i ++ ) {
            View item = container.getChildAt(i);
            if( item instanceof ImageItem ) {
                ImageItem imageItem = (ImageItem)item;
                if( imageItem.getEditable() ) {
                    listItens.put((imageItem).getTagId(), imageItem);
                }
            }
        }

        return listItens;
    }

    public static Integer getItemIndex(Activity activity, ImageItem imgItem) {
        //Recupera as imagens
        GridLayout container = (GridLayout)activity.findViewById(R.id.capture);
        for( int i = 0; i < container.getChildCount(); i ++ ) {
            View item = container.getChildAt(i);
            if( item instanceof ImageItem ) {
                ImageItem imageItem = (ImageItem)item;
                if( imageItem.equals(imgItem) ) {
                    return i;
                }
            }
        }

        return null;
    }

}