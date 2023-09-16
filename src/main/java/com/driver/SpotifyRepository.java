package com.driver;

import java.util.*;

import jdk.jshell.ExpressionSnippet;
import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;    // use
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public  User createUser(String name, String mobile) {
        // create an user and add the user to user list.
        User Per=new User(name,mobile);
        users.add(Per);
        return Per;
    }

    public Artist createArtist(String name) {
        // create an artist object
        Artist art=new Artist(name);
        // Add this artist to list of artists
        artists.add(art);
        return  art;

    }

    public Album createAlbum(String title, String artistName) {
        // First checkk if already album is present with this name

        Album alb=null;
        for(Album a:albums){
            if(a.getTitle().equals(title)){
                alb=a;
                break;
            }
        }
           if(alb!=null)return alb;
           else alb=new Album(title);
        // Add album to album list
        albums.add(alb);
        // Now check for the artist in artist list
        Artist PresentArtist=null;
        for(Artist composer:artists){
            if(composer.getName().equals(artistName)){
                PresentArtist=composer;
                break;
            }
        }
          List<Album>AlbumListFoArtist=new ArrayList<>();
        if(PresentArtist!=null){
            AlbumListFoArtist= artistAlbumMap.getOrDefault(PresentArtist,new ArrayList<>());
            AlbumListFoArtist.add(alb);
        }else{
            PresentArtist=new Artist(artistName);
            AlbumListFoArtist.add(alb);
            // Add the new created Artist to artist list
            artists.add(PresentArtist);

        }
        artistAlbumMap.put(PresentArtist,AlbumListFoArtist);
             return alb;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        // First check album is present or not, if it then add the song to the album.
        Song NewSong=new Song(title,length);

        Album PresentAlbum=null;
        for(Album alb:albums){
            if(alb.getTitle().equals(albumName)){
                PresentAlbum=alb;
                break;
            }
        }
           if(PresentAlbum!=null){
               // Means contains album, so add the song too the album list
               List<Song>SongListForPresentAlbum=albumSongMap.getOrDefault(PresentAlbum,new ArrayList<>());
               // Add the song to the songllist
               SongListForPresentAlbum.add(NewSong);
               // update hashmap again with the added new song
               albumSongMap.put(PresentAlbum,SongListForPresentAlbum);


           }else{
               throw new CustomException("Album does not exist");
           }
           return NewSong;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
//        check for the avalability of the playlist
        Playlist PresentPlayList=null;
        boolean newPlaylist=true;

        for(Playlist Plist:playlists){
            if(Plist.getTitle().equals(title)){
                PresentPlayList=Plist;
                newPlaylist=false;
                break;
            }
        }
        if(PresentPlayList==null) {
            PresentPlayList = new Playlist(title);
        }else{
            // Playlist with already title is present in database
            return PresentPlayList;
        }
        //Checking for user is present or not

        User Presentuser=null;
        //check the given user is present or not,if present  make the playlist owner as user
        for(User Person:users) {
            if (Person.getMobile().equals(mobile)) {
                Presentuser = Person;
                break;
            }
        }
            if(Presentuser==null){throw new CustomException("User does not exist");}
                //Add the playlist to list of playlists

                // User is present in database list
                // Select all the songs from the songslist, and add it to play list
                List<Song> songslistforPresentPlaylist = playlistSongMap.getOrDefault(PresentPlayList, new ArrayList<>());

                // traverse through song list, and add the song with same length to playlist songs
                for (Song s : songs) {
                    if (s.getLength() == length && !songslistforPresentPlaylist.contains(s)) {
                        // add the song to playlist song list
                        songslistforPresentPlaylist.add(s);
                    }
                }
                // Now store the playlist, user, songslist in hashmap databases
//                Now check if presentPlaylist is already created by some user or it is freshly created by the present user
                if (newPlaylist && !creatorPlaylistMap.containsKey(Presentuser)) {
                    playlistSongMap.put(PresentPlayList, songslistforPresentPlaylist);

                    // Its confirmed that this playlist is freshly created by the present user
                    creatorPlaylistMap.put(Presentuser, PresentPlayList);
                    playlists.add(PresentPlayList);


                List<Playlist> UserslistOfPlaylist;
                UserslistOfPlaylist = userPlaylistMap.getOrDefault(Presentuser, new ArrayList<>());
                if (!UserslistOfPlaylist.contains(PresentPlayList)) {
                    // add only if this user did not use the playlist,till now
                    UserslistOfPlaylist.add(PresentPlayList);
                    userPlaylistMap.put(Presentuser, UserslistOfPlaylist);
                }
                // check the user listened to this playlist everbefore,and add accordingly

                List<User> PlayListListenersList;
                PlayListListenersList = playlistListenerMap.getOrDefault(PresentPlayList, new ArrayList<>());
                if (!PlayListListenersList.contains(Presentuser)) {
                    PlayListListenersList.add(Presentuser);
                    playlistListenerMap.put(PresentPlayList, PlayListListenersList);
                }

            }


            return PresentPlayList;

    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        // check if the playlist is already present in database
        Playlist PresentPlayList=null;
        boolean IsNewPlaylist=true;
        for(Playlist playlist:playlists){
            if(playlist.getTitle().equals(title)){
                PresentPlayList=playlist;
                IsNewPlaylist=false;
                break;
            }
        }
        if(PresentPlayList==null){
            PresentPlayList=new Playlist(title);
        }else{
            // Playlist is already present in database,cannot create playlist with same name
            return PresentPlayList;
        }
        User Presentuser=null;
        //check for the user is present in db or not
        for(User consumer:users){
            if(consumer.getMobile()==mobile){
                Presentuser=consumer;
                break;
            }
        }

        // if user not found return with exception
        if(Presentuser==null){throw new CustomException("User does not exist");}
//            user is present in database
            // Add the songs to the playlist
            List<Song> SongsListForPresentPlayList;
            SongsListForPresentPlayList = playlistSongMap.getOrDefault(PresentPlayList, new ArrayList<>());

            for (Song s : songs) {
                if (songTitles.contains(s.getTitle()) && !SongsListForPresentPlayList.contains(s)) {
                    SongsListForPresentPlayList.add(s);
                }
            }
            // keeep playlistSongMap hashmap updated with playlist and songlist

            if (IsNewPlaylist==true&&!creatorPlaylistMap.containsKey(Presentuser)) {
                playlistSongMap.put(PresentPlayList, SongsListForPresentPlayList);

                // In this case, the new playlist created by this user only
//                update this in create Hashmap

                creatorPlaylistMap.put(Presentuser, PresentPlayList);
                playlists.add(PresentPlayList);


                List<Playlist> ListOfPlayListsForUser;
                ListOfPlayListsForUser = userPlaylistMap.getOrDefault(Presentuser, new ArrayList<>());
                if (!ListOfPlayListsForUser.contains(PresentPlayList)) {
                    ListOfPlayListsForUser.add(PresentPlayList);
                }
                userPlaylistMap.put(Presentuser, ListOfPlayListsForUser);

                List<User> ListOfListersForPresentPlayList;
                ListOfListersForPresentPlayList = playlistListenerMap.getOrDefault(PresentPlayList, new ArrayList<>());
                if (!ListOfListersForPresentPlayList.contains(Presentuser)) {
                    ListOfListersForPresentPlayList.add(Presentuser);
                }
                playlistListenerMap.put(PresentPlayList, ListOfListersForPresentPlayList);
            }


            return PresentPlayList;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        // check if user is presen ornot, throw exception according
        User PresentUser=null;

        for(User consumer:users){
            if(consumer.getMobile().equals(mobile)){
               PresentUser=consumer;
               break;
            }
        }
        if(PresentUser==null)throw new CustomException("User does not exist");

        Playlist PresentPlaylist=null;
        // check for playlist is there ornot
        for(Playlist Plist:playlists){
            if(Plist.getTitle().equals(playlistTitle)){
                PresentPlaylist=Plist;
                break;
            }
        }
        if(PresentPlaylist==null)throw new CustomException("Playlist does not exist");

        // Check if user itself created the presentplaylist,, if it is,then listener,and user already while creating
        if(creatorPlaylistMap.containsKey(PresentUser)&&creatorPlaylistMap.get(PresentUser).equals(PresentPlaylist)){
            // This is already added while creating the playlist by this user
        }
        else{
            // Present user, will be the user for this playlist
            List<Playlist>userPlaylist;
            userPlaylist = userPlaylistMap.getOrDefault(PresentUser,new ArrayList<>());
            if(!userPlaylist.contains(PresentPlaylist)){
                userPlaylist.add(PresentPlaylist);
                userPlaylistMap.put(PresentUser,userPlaylist);
            }
            List<User>userListForPresentPlaylist;
            userListForPresentPlaylist=playlistListenerMap.getOrDefault(PresentPlaylist,new ArrayList<>());
            if(userListForPresentPlaylist.contains(PresentUser)){
                userListForPresentPlaylist.add(PresentUser);
                playlistListenerMap.put(PresentPlaylist,userListForPresentPlaylist);
            }
        }
        return PresentPlaylist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        // Search for user, if didnot find user,throw exception
        User PresentUser=null;
        for(User Person:users){
            if(Person.getMobile().equals(mobile)){
                PresentUser=Person;
                break;
            }
        }
        if(PresentUser==null)throw new CustomException("User does not exist");

        // Now check for the song present in songlist, if not throw exception
        Song PresentSong=null;
        for(Song s:songs){
            if(s.getTitle().equals(songTitle)){
                PresentSong=s;
                break;
            }
        }
        if(PresentSong==null)throw new CustomException("Song does not exist");

        List<User>UsersLikedPresentSong;
        UsersLikedPresentSong = songLikeMap.getOrDefault(PresentSong,new ArrayList<>());
        if(!UsersLikedPresentSong.contains(PresentUser)){
            UsersLikedPresentSong.add(PresentUser);
            songLikeMap.put(PresentSong,UsersLikedPresentSong);

            //Now like the artist who composed the song
            // Go the every album and search for this song, and find the album
            // Go the every artist and search fro this album and find the artist
            // like his work
            Album album=null;
            for(Map.Entry<Album,List<Song>>mapElement:albumSongMap.entrySet()){
                List<Song>songsListForPresentAlbum=mapElement.getValue();
                if(songsListForPresentAlbum.contains(PresentSong)){
                    album=mapElement.getKey();
                    break;
                }
            }
            // Then Go to every artist and find the album
            Artist ComposerOfPresentSong=null;
            for(Map.Entry<Artist,List<Album>> mapElement:artistAlbumMap.entrySet()){
                List<Album>AlbumListForArtist=mapElement.getValue();
                if(AlbumListForArtist.contains(album)){
                    ComposerOfPresentSong=mapElement.getKey();
                    break;
                }
            }
            int likes=ComposerOfPresentSong.getLikes()+1;
            ComposerOfPresentSong.setLikes(likes);

        }

//        public HashMap<Artist, List<Album>> artistAlbumMap;
//        public HashMap<Album, List<Song>> albumSongMap;
          return PresentSong;
    }

    public String mostPopularArtist() {
        // Traverse through all the artists and find the maximum number of likes
        int MaxLikes=0;
        String Composer_Name=null;
        for(Artist composer:artists){
            if(composer.getLikes()>MaxLikes){
                MaxLikes=composer.getLikes();
                Composer_Name= composer.getName();
            }
        }
             return Composer_Name;
    }

    public String mostPopularSong() {
        int Maxlikes=0;
        String songName=null;
        //Traverse through the songlike map and find the max likes for a song in hm
        for(Map.Entry<Song,List<User>>mapElement:songLikeMap.entrySet()){
            List<User>UserListForSong=mapElement.getValue();
            if(UserListForSong.size()>Maxlikes){
                Maxlikes=UserListForSong.size();
                songName=mapElement.getKey().getTitle();
            }
        }
        return songName;

    }
}
