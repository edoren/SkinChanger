package me.edoren.skin_changer.common.messages;

@SuppressWarnings("unused")
public class PlayerDBModel {
    public Data data;
    public Boolean success;

    public static class Data {
        public Player player;

        public static class Player {
            public String id;
        }
    }
}
