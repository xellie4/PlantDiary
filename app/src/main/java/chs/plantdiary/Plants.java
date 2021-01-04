package chs.plantdiary;

public class Plants {
    private String plantName;
    private String sun;
    private String water;
    private String temp;
    private String fertilizer;
    private String soil;
    private String imageUrl;

    public Plants(){
        // empty constructor needed
    }

    public Plants(String plantName, String sun, String water, String temp, String fertilizer, String soil,String imageUrl){
        if(plantName.trim().equals("")){
            this.plantName = "No name";
        }

        if(sun.trim().equals("")){
            this.sun = "No data entered";
        }

        if(water.trim().equals("")){
            this.water = "No data entered";
        }
        if(temp.trim().equals("")){
            this.temp = "No data entered";
        }

        if(fertilizer.trim().equals("")){
            this.fertilizer = "No data entered";
        }

        if(soil.trim().equals("")){
            this.soil = "No data entered";
        }

        this.plantName = plantName;
        this.sun = sun;
        this.water = water;
        this.temp = temp;
        this.fertilizer = fertilizer;
        this.soil = soil;
        this.imageUrl = imageUrl;
    }

    //getter and setter for plant name
    public String getPlantName(){
        return plantName;
    }

    public void setPlantName(String plantName){
        this.plantName = plantName;
    }

    //getter and setter for sun
    public String getSun(){
        return sun;
    }

    public void setSun(String sun){
        this.sun = sun;
    }

    //getter and setter for water
    public String getWater(){
        return water;
    }

    public void setWater(String water){
        this.water = water;
    }

    //getter and setter for temp
    public String getTemp(){
        return temp;
    }

    public void setTemp(String temp){
        this.temp = temp;
    }

    //getter and setter for fertilizer
    public String getFertilizer(){
        return fertilizer;
    }

    public void setFertilizer(String fertilizer){
        this.fertilizer = fertilizer;
    }

    //getter and setter for soil
    public String getSoil(){
        return soil;
    }

    public void setSoil(String soil){
        this.soil = soil;
    }

    //getter and  setter for image url
    public String getImageUrl(){
        return imageUrl;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

}
