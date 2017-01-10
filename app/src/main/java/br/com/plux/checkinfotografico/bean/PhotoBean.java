package br.com.plux.checkinfotografico.bean;

/**
 * Created by gustavonobrega on 01/06/2016.
 */
public class PhotoBean {
    private Long id;
    private String file;
    private String campaign;
    private int id_campaign;
    private int id_user;
    private int id_location;
    private int key_grid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Integer getId_campaign() {
        return id_campaign;
    }

    public void setId_campaign(int id_campaign) {
        this.id_campaign = id_campaign;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public int getId_location() {
        return id_location;
    }

    public void setId_location(int id_location) {
        this.id_location = id_location;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public int getKey_grid() {
        return key_grid;
    }

    public void setKey_grid(int key_grid) {
        this.key_grid = key_grid;
    }
}
