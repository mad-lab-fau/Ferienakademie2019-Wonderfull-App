package de.ferienakademie.wonderfull;

public class ProfileValues {

    public enum Fitness {
        Profi("Profi"),
        Gelegenheit ("Gelegenheit"),
        Anfänger ("Anfänger");

        private String name = "";

        Fitness(String name){
            this.name = name;
        }

        public String getName(){
            return this.name;
        }
    }

    private String name = "";
    private String surname = "";
    private float size;
    private float weight;
    private String diseases = "";
    private String medication = "";
    private String allergies = "";
    private Fitness fitness = Fitness.Gelegenheit;

    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String SIZE = "size";
    public static final String WEIGHT = "weight";
    public static final String DISEASES = "diseases";
    public static final String MEDICATION = "medication";
    public static final String ALLERGIES = "allergies";
    public static final String FITNESS = "fitness";


    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public float getSize() {
        return size;
    }

    public String getSizeString(){
        return Float.toString(size);
    }

    public float getWeight() {
        return weight;
    }

    public String getWeightString(){
        return Float.toString(weight);
    }

    public String getAllergies() {
        return allergies;
    }

    public String getDiseases() {
        return diseases;
    }

    public String getMedication() {
        return medication;
    }

    public String getFitness(){ return fitness.getName(); }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public void setDiseases(String diseases) {
        this.diseases = diseases;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public void setFitness(String fitness){
        this.fitness = Fitness.valueOf(fitness);
    }

    public void setFitness(Fitness fitness){
        this.fitness = fitness;
    }

    public int fitnessToInt(){
        switch (fitness){
            case Profi:
                return 0;
            case Gelegenheit:
                return 1;
            case Anfänger:
                return 2;
            default:
                return -1;
        }
    }

    public Fitness intToFitness(int num){
        switch (num){
            case 0:
                return Fitness.Profi;
            case 1:
                return Fitness.Gelegenheit;
            case 2:
                return Fitness.Anfänger;
            default:
                return null;
        }
    }

}
