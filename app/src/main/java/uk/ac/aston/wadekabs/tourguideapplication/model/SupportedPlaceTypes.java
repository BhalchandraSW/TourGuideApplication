package uk.ac.aston.wadekabs.tourguideapplication.model;

import java.io.Serializable;

/**
 * Created by bhalchandrawadekar on 09/03/2017.
 */

public class SupportedPlaceTypes implements Serializable {

    private boolean lodging;
    private boolean locality;
    private boolean political;
    private boolean establishment;
    private boolean food, restaurant, bar, gym, health, spa;
    private boolean point_of_interest;

    public boolean isLodging() {
        return lodging;
    }

    public void setLodging(boolean lodging) {
        this.lodging = lodging;
    }

    public boolean isLocality() {
        return locality;
    }

    public void setLocality(boolean locality) {
        this.locality = locality;
    }

    public boolean isPolitical() {
        return political;
    }

    public void setPolitical(boolean political) {
        this.political = political;
    }

    public boolean isEstablishment() {
        return establishment;
    }

    public void setEstablishment(boolean establishment) {
        this.establishment = establishment;
    }

    public boolean isPoint_of_interest() {
        return point_of_interest;
    }

    public void setPoint_of_interest(boolean point_of_interest) {
        this.point_of_interest = point_of_interest;
    }

    public boolean isFood() {
        return food;
    }

    public void setFood(boolean food) {
        this.food = food;
    }

    public boolean isRestaurant() {
        return restaurant;
    }

    public void setRestaurant(boolean restaurant) {
        this.restaurant = restaurant;
    }

    public boolean isBar() {
        return bar;
    }

    public void setBar(boolean bar) {
        this.bar = bar;
    }

    public boolean isGym() {
        return gym;
    }

    public void setGym(boolean gym) {
        this.gym = gym;
    }

    public boolean isHealth() {
        return health;
    }

    public void setHealth(boolean health) {
        this.health = health;
    }

    public boolean isSpa() {
        return spa;
    }

    public void setSpa(boolean spa) {
        this.spa = spa;
    }
}
