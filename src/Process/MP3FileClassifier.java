package Process;

import UI.ProgressFrame;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class MP3FileClassifier extends Thread {
    public final static boolean PRINT_PROCESS = false;

    ArrayList<MP3File> MP3FileList = new ArrayList<MP3File>();

    String sourceDir;
    String destDir;

    ProgressFrame progressFrame;

    public MP3FileClassifier(String sd, String dd) {
        sourceDir = sd;
        destDir = dd;
        setMP3FileList(new File(sourceDir));

        System.out.println("MP3FileList.size(): " + MP3FileList.size());

        if (MP3FileList.size() == 0) {//폴더에 파일이 없으면
            System.exit(0);
        }

        progressFrame = new ProgressFrame(MP3FileList.size());

        start();
    }

    public void run() {
        MP3InfoManager mim = new MP3InfoManager();

        for (MP3File MF : MP3FileList) {
            if (mim.openMP3(MF.path)) {

                System.out.println(mim.showALL()); // DREW

                //메타 데이터 추출
                MF.albumName = mim.getAlbum();
                MF.artist = mim.getArtist();
                MF.titleName = mim.getTitle();
                MF.trackNumber = mim.getTrackNumber();

                byte[] newFileData = null;
                String rightArtistName = mim.getRightArtistName(MF.artist);

                if (rightArtistName != null) { //가수 명 변경 후 파일 생성
                    MF.artist = rightArtistName;
                    newFileData = mim.getNewFileData(MF.artist);
                }

                mim.closeMP3();

                //파일명 생성 (경로 포함 x)
                MF.fileName = "" + MF.trackNumber + "-" + MF.titleName + ".mp3";

                handleFile(MF, newFileData);

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
        MP3FileList.clear();
    }

    void setMP3FileList(File file) {

        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            // 디렉토리면 자식요소를 뽑아 반복하여 재귀호출
            String[] files = file.list();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    setMP3FileList(new File(file, files[i]));
                }
            }
        } else {
            // 파일이면 count 증가
            try {
                String path = file.getCanonicalPath();
                //file.
                if ((path.endsWith(".mp3"))) {
                    MP3FileList.add(new MP3File(path));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void handleFile(MP3File mp, byte[] newFileData) {

        try {
            //디렉토리 명 설정
            mp.artist = replaceBadWords(mp.artist);
            mp.albumName = replaceBadWords(mp.albumName);
            mp.fileName = replaceBadWords(mp.fileName);

            if (PRINT_PROCESS) {
                System.out.println("change: " + mp.albumName);
                System.out.println("change: " + mp.artist);
                System.out.println("change: " + mp.fileName);
            }

            String dir = destDir + "\\" + mp.artist + "\\" + mp.albumName;

            //디렉토리 생성
            new File(dir).mkdirs();

            String dest = dir + "\\" + mp.fileName;

            if (newFileData == null) {
                //Files.move(Paths.get(mp.path), Paths.get(dest), StandardCopyOption.ATOMIC_MOVE);
                Files.copy(Paths.get(mp.path), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.write(Paths.get(dest), newFileData, StandardOpenOption.CREATE);
            }

            System.out.println("DONE: " + mp.path + "\n\t-> " + dest);

        } catch (Exception e1) {
            e1.printStackTrace();
            System.out.println("FAIL: " + mp.path);
        }
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