package Process;

import UI.ProgressFrame;

import java.io.File;
import java.util.ArrayList;

public class MP3FileClassifier extends Thread {
    public final static boolean PRINT_PROCESS = false;

    ArrayList<MP3FileMeta> MP3FileMetaList = new ArrayList<MP3FileMeta>();

    String sourceDir;
    String destDir;

    ProgressFrame progressFrame;

    public MP3FileClassifier(String sd, String dd) {
        sourceDir = sd;
        destDir = dd;
        setMP3FileMetaList(new File(sourceDir));

        System.out.println("MP3FileMetaList.size(): " + MP3FileMetaList.size());

        if (MP3FileMetaList.size() == 0) {//폴더에 파일이 없으면
            System.exit(0);
        }

        progressFrame = new ProgressFrame(MP3FileMetaList.size());

        start();
        progressFrame.start();
    }

    public void run() {
        MP3InfoManager mim = new MP3InfoManager();

        for (MP3FileMeta meta : MP3FileMetaList) {
            if (mim.openMP3(meta.path)) {

                //메타 데이터 추출
                meta.albumName = mim.getAlbum();
                meta.artist = mim.getArtist();
                meta.titleName = mim.getTitle();
                meta.trackNumber = mim.getTrackNumber();

                String rightArtistName = mim.getRightArtistName(meta.artist);

                if (rightArtistName != null) { //가수 명 변경 후 파일 생성
                    meta.artist = rightArtistName;
                }

                //파일명 생성 (경로 포함 x)
                meta.fileName = "" + meta.trackNumber + "-" + meta.titleName + ".mp3";

                saveMp3File(meta, mim);

                ProgressFrame.N_finishedFile++;
            }
        }

        //종료
        progressFrame.setEnabled(true);
        progressFrame.setVisible(false);
        closeAll();

        System.out.println("Process Done");
        System.exit(0);
    }

    void closeAll() {
        MP3FileMetaList.clear();
    }

    void setMP3FileMetaList(File file) {

        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            // 디렉토리면 자식요소를 뽑아 반복하여 재귀호출
            String[] files = file.list();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    setMP3FileMetaList(new File(file, files[i]));
                }
            }
        } else {
            // 파일이면 count 증가
            try {
                String path = file.getCanonicalPath();
                //file.
                if ((path.endsWith(".mp3"))) {
                    MP3FileMetaList.add(new MP3FileMeta(path));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void saveMp3File(MP3FileMeta meta, MP3InfoManager mim) {

        try {
            String dir = getDir(meta);
            new File(dir).mkdirs();

            String dest = dir + "\\" + meta.fileName;
            mim.setMetadata(meta, dest);

            System.out.println("DONE: " + meta.path + "\n\t-> " + dest);

        } catch (Exception e1) {
            e1.printStackTrace();
            System.out.println("FAIL: " + meta.path);
        }
    }

    private String getDir(MP3FileMeta meta) {
        //디렉토리 명 설정
        meta.artist = replaceBadWords(meta.artist);
        meta.albumName = replaceBadWords(meta.albumName);
        meta.fileName = replaceBadWords(meta.fileName);

        if (PRINT_PROCESS) {
            System.out.println("change: " + meta.albumName);
            System.out.println("change: " + meta.artist);
            System.out.println("change: " + meta.fileName);
        }

        return destDir + "\\" + meta.artist + "\\" + meta.albumName;
    }

    String replaceBadWords(String str) {
        str = str.replace('/', '_');
        str = str.replace('\\', '_');
        str = str.replace(':', '_');
        str = str.replace('?', '_');
        str = str.replace('*', '_');
        str = str.replace('"', '_');
        str = str.replace('>', '_');
        str = str.replace('<', '_');
        str = str.replace('|', '_');

        return str;
    }

}