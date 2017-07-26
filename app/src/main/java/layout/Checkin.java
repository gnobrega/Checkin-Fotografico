package layout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public static ViewGroup rootViewStatic;
    public HashMap<String, Intent> listImage = new HashMap<String, Intent>();
    public static int countImg = 0;
    public static HashMap<Integer, HashMap> listImages = new HashMap<Integer, HashMap>();
    FrameLayout checkinContainer;
    Menu menu = null;
    public Context context = null;
    public static Integer currentGrid = -1;
    public static Integer lastGrid = null;
    private Integer totalFiles = 0;

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
        Checkin.lastGrid = 0;

        //Verifica se o aparelho possui a permissão de escrita no sdcard
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Integer PERMISSIONS_REQUEST_WRITE = 1;
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE);
        }

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
                novaImagem.setImageBitmap(Util.resizeImage(context, bitmap, App.THUMB_WIDTH));
                novaImagem.setPadding(5, 5, 5, 5);
                novaImagem.setTag(tagId);
                novaImagem.setLongClickable(true);

                //Monta o objeto ImageItem
                ImageItem imageItem = new ImageItem(context, activity);
                imageItem.setIdDb(photoBean.getId());
                imageItem.setTagId(tagId);
                imageItem.addView(novaImagem);
                imageItem.setRealFile(photoBean.getFile());
                imageItem.setKeyGrid(photoBean.getKey_grid());

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
                    HashMap<String, ImageItem> lstImages = new LinkedHashMap<>();
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

    public void refreshFragment() {
        FragmentTransaction tx = ((MainActivity)App.MAIN_ACTIVITY).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.flContent, Fragment.instantiate(App.MAIN_ACTIVITY, "layout.Checkin"));
        tx.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        final HashMap<String, ImageItem> listImages = Checkin.getItensSelected(getActivity());
        final Checkin checkin = this;

        //Adiciona uma nova tela
        if(id == R.id.action_attr_nova_tela) {
            GridLayout lastGrid = getLastGrid();
            if( lastGrid != null && lastGrid.getChildCount() == 0 ) {
                //Impede inserir várias grids vazias
            } else {
                Checkin.currentGrid ++;
                adicionarGrid();
            }

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

                                //Regarrega a grid
                                refreshFragment();
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

    public Integer countGrids() {
        LinearLayout containerGrid = (LinearLayout)rootView.findViewById(R.id.containerGrid);
        Integer total = 0;
        if( containerGrid != null ) {
            for (Integer i = 0; i < containerGrid.getChildCount(); i++) {
                View item = containerGrid.getChildAt(i);
                if (item instanceof GridLayout) {
                    total ++;
                }
            }
        } else {
            return null;
        }
        return total;
    }

    public void adicionarGrid() {
        LinearLayout containerGrid = (LinearLayout)rootView.findViewById(R.id.containerGrid);
        GridLayout lastGrid = getLastGrid();
        if( lastGrid != null && lastGrid.getChildCount() == 0 ) {
            return;
        }

        GridLayout grid = new GridLayout(App.MAIN_ACTIVITY);
        grid.setColumnCount(App.getGridCols());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , 5);
        lp.setMargins(0, 10, 0, 0);
        View sep = new LinearLayout(App.MAIN_ACTIVITY);
        sep.setLayoutParams(lp);
        sep.setBackgroundColor(Color.GRAY);

        containerGrid.addView(grid);
        containerGrid.addView(sep);

        Log.i("*LOG*", "Nova grid adicionada. Total: " + countGrids() + " || Grid atual: " + Checkin.currentGrid);
    }

    public static GridLayout getLastGrid() {
        if( Checkin.rootViewStatic != null ) {
            LinearLayout containerGrid = (LinearLayout) Checkin.rootViewStatic.findViewById(R.id.containerGrid);
            GridLayout grid = null;
            if (containerGrid != null) {
                for (Integer i = 0; i < containerGrid.getChildCount(); i++) {
                    View item = containerGrid.getChildAt(i);
                    if (item instanceof GridLayout) {
                        grid = (GridLayout) item;
                    }
                }
            }
            return grid;
        } else {
            return null;
        }
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
        Checkin.rootViewStatic = rootView;
        checkinContainer = (FrameLayout)rootView.findViewById(R.id.checkinContent);

        //Cria a primeira grid
        LinearLayout containerGrid = (LinearLayout)Checkin.rootViewStatic.findViewById(R.id.containerGrid);
        if( containerGrid.getChildCount() == 0 ) {
            adicionarGrid();
        }
        if( Checkin.currentGrid == -1 ) {
            Checkin.currentGrid = 0;
        }
        Log.i("*LOG*", "onCreateView() || Checkin.currentGrid: "+Checkin.currentGrid+" || countGrids(): " + countGrids());

        //Clique no botão da câmera
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lstUrlFileCamera.clear();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                openCam();
            }
        });

        //Limpa a grid
        GridLayout grid = getLastGrid();
        Checkin.lastGrid = 0;

        //Exibe o ponto selecionado
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("location", Context.MODE_PRIVATE);
        Integer locationId = sharedpreferences.getInt("id", 0);
        if (sharedpreferences != null && locationId != 0) {
            TextView locationName = (TextView) rootView.findViewById(R.id.checkinLocationName);
            locationName.append(sharedpreferences.getString("name", ""));

            //Devolve as imagens já cadastradas à grid
            if (Checkin.listImages.containsKey(locationId) && Checkin.listImages.get(locationId) != null) {
                HashMap<String, ImageItem> lstImages = Checkin.listImages.get(locationId);

                //Container
                grid.removeAllViews();

                for (String tagId : lstImages.keySet()) {
                    ImageItem imageItem = (ImageItem) lstImages.get(tagId);
                    if (imageItem.getParent() != null) {
                        ((GridLayout) imageItem.getParent()).removeView(imageItem);
                    }
                    if (Checkin.lastGrid != imageItem.getKeyGrid()) {
                        Checkin.lastGrid = imageItem.getKeyGrid();
                        adicionarGrid();
                        grid = getLastGrid();
                    }

                    grid.addView(imageItem);
                }

                //Mantém a última grid vazia caso exista
                if( Checkin.currentGrid == -1 ) {
                    Checkin.currentGrid = countGrids() - 1;
                }
                if( Checkin.currentGrid >= countGrids() ) {
                    adicionarGrid();
                }

                //Log.i("*LOG*", "onCreateView() || Total de grids em uso: " + countGrids() + " || Total de grids adicionadas: " + Checkin.currentGrid);
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

        totalFiles = countFiles();
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        intent = Intent.createChooser(intent, "Select Camera");

        intent.putExtra("android.intent.extra.quickCapture", true);
        String pathPhotos = App.PATH_PHOTOS;

        File dir = new File(pathPhotos);
        if( !dir.exists() ) {
            dir.mkdir();
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
        switch (resquestCode) {

            case 0:
                exitingCamera();
                break;
        }

    }

    /****** Utilizado para tirar fotografias em sequência ******/
    private Integer countFiles() {
        Cursor cursor = loadCursorFiles();
        return cursor.getCount();
    }
    private void exitingCamera() {
        Cursor cursor = loadCursorFiles();
        String[] paths = getImagePaths(cursor, totalFiles);
        cursor.close();
        if( paths != null ) {
            for (String path : paths) {
                guardarImagensCamera(path);
            }
        }
        tratarImagensCamera();
    }
    public Cursor loadCursorFiles() {

        final String[] columns = { MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID };

        final String orderBy = MediaStore.Images.Media.DATE_ADDED;

        return App.MAIN_ACTIVITY.getApplicationContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy);
    }
    public String[] getImagePaths(Cursor cursor, int startPosition) {
        int size = cursor.getCount() - startPosition;
        if (size <= 0)
            return null;
        String[] paths = new String[size];
        int dataColumnIndex = cursor
                .getColumnIndex(MediaStore.Images.Media.DATA);
        for (int i = startPosition; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            paths[i - startPosition] = cursor.getString(dataColumnIndex);
        }
        return paths;
    }
    /**************************************************************/

    /**
     * Adiciona a imagem ao array
     */
    public List<String> lstUrlFileCamera = new ArrayList<>();
    //public void guardarImagensCamera(Intent data) {
    public void guardarImagensCamera(String urlFile) {

        File photo = new File(urlFile);
        if( photo.exists() ) {

            //Realoca a imagem
            int pos = urlFile.lastIndexOf("/");
            String newPhoto = App.PATH_PHOTOS + urlFile.substring(pos);
            File newPhotoFile = new File(newPhoto);
            Util.moveFile(photo, newPhotoFile);
            if( newPhotoFile.exists() ) {
                //Recupera o bitmap
                //bitmap = MediaStore.Images.Media.getBitmap(App.MAIN_ACTIVITY.getContentResolver(), mImageUri);
                lstUrlFileCamera.add(newPhoto);
            }
        } else {
            Log.e("Log", "Imagem não encontrada: " + urlFile);
        }
    }

    public void tratarImagensCamera() {

        for( int i = 0; i < lstUrlFileCamera.size(); i ++ ) {
            urlFile = lstUrlFileCamera.get(i);
            Bitmap bitmap = Util.getBitmapFile(urlFile);

            countImg ++;
            String tagId = "image"+ countImg;

            //Comprime a imagem
            Util.compressImage(App.MAIN_ACTIVITY.getApplicationContext(), urlFile);

            //Adiciona ao banco de dados
            DataBase dataBase = new DataBase(getContext());
            Long photoDbId = dataBase.insertPhoto(urlFile, null, "", Util.getUserSession(App.MAIN_ACTIVITY.getApplicationContext()).getId(), getLocation(), Checkin.currentGrid);

            //Adiciona à grid
            addGridImg(bitmap, tagId, urlFile, photoDbId);
        }
    }

    /**
     * Adiciona a imagem à grid
     */
    public void addGridImg(Bitmap bitmap, String tagId, String urlFile, Long photoDbId) {

        //Container
        GridLayout container = getLastGrid();
        container.setColumnCount(App.getGridCols());

        //Cria um novo item de imagem
        ImageItem imageItem = new ImageItem(getContext(), this);
        final ImageView novaImagem = new ImageView(getContext());
        novaImagem.setImageBitmap(Util.resizeImage(rootView.getContext(), bitmap, App.THUMB_WIDTH));
        novaImagem.setPadding(5, 5, 5, 5);
        novaImagem.setTag(tagId);
        novaImagem.setLongClickable(true);
        //imageItem.setUri(uri);
        imageItem.setTagId(tagId);
        //imageItem.setData(data);
        imageItem.addView(novaImagem);
        imageItem.setRealFile(urlFile);
        imageItem.setIdDb(photoDbId);
        imageItem.setKeyGrid(Checkin.currentGrid);
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
        GridLayout container = getLastGrid();

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
        HashMap<String, ImageItem> listItens = new HashMap<String, ImageItem>();

        LinearLayout containerGrid = (LinearLayout)Checkin.rootViewStatic.findViewById(R.id.containerGrid);
        GridLayout grid = null;
        if( containerGrid != null ) {
            for (Integer i = 0; i < containerGrid.getChildCount(); i++) {
                View itemG = containerGrid.getChildAt(i);
                if( itemG instanceof GridLayout ) {
                    grid = (GridLayout)itemG;

                    for( int j = 0; j < grid.getChildCount(); j ++ ) {
                        View item = grid.getChildAt(j);
                        if( item instanceof ImageItem ) {
                            ImageItem imageItem = (ImageItem)item;
                            if( imageItem.getEditable() ) {
                                listItens.put((imageItem).getTagId(), imageItem);
                            }
                        }
                    }
                }
            }
        }

        return listItens;
    }

    public static Integer getItemIndex(Activity activity, ImageItem imgItem) {
        //Recupera as imagens
        GridLayout container = Checkin.getLastGrid();
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

