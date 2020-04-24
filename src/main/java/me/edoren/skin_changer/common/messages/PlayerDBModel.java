package me.edoren.skin_changer.common.messages;

public class PlayerDBModel {
    public static class Data {
        public static class Player {
            public String id;
        }

        public Player player;
    }

    public Data data;
    public Boolean success;
}
