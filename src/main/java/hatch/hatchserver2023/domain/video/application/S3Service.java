package hatch.hatchserver2023.domain.video.application;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.S3StatusCode;
import hatch.hatchserver2023.global.common.response.exception.S3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// TODO: public으로 안올라가면 어떡하지
@Slf4j
@Service
public class S3Service {

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucket;

    private final AmazonS3 amazonS3;


    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }


    // TODO: 로그인한 user 추가

    /**
     * 다수의 파일 업로드
     * @param multipartFileList
     * @return urlList
     */
    public List<String> upload(List<MultipartFile> multipartFileList) {
        log.info("[S3SERVICE] S3 upload - multi files");
        List<String> urlList = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFileList) {
            urlList.add(upload(multipartFile));
        }
        return urlList;
    }

    /**
     * 하나의 파일 업로드
     * @param multipartFile
     * @return url
     */
    public String upload(MultipartFile multipartFile) {
        log.info("[S3SERVICE] S3 upload - single file");

        // TODO: 파일 경로가 /video/{user uuid}/(원래 랜덤값 + 파일명) 되도록 만들기
        // TODO: user를 다루는 방법은 이게 맞을까? controller에서 uuid만 받아올까?
        String fileName = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename(); //랜덤값 + 원래 파일명추가해서 이름 설정(안겹치도록)
//        String path = "/video/" + user.getUuid() + "/"+fileName;
        String path = "video/"+fileName;
        try {
            //file로 변환해 임시로 로컬에 저장
            File file = convertMultipartFileToSavedFile(multipartFile)
                    .orElseThrow(() -> new S3Exception(S3StatusCode.FILE_CONVERT_FAIL));

            //S3 업로드
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, path, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(putObjectRequest);

            //임시 저장한 파일 삭제
            removeTempFile(file);

        } catch (IOException e) {
            log.error("[FAIL] file upload fail");
            e.printStackTrace();
            throw new S3Exception(S3StatusCode.S3_FILE_UPLOAD_FAIL);
        }

        return amazonS3.getUrl(bucket, path).toString(); //업로드된 url 가져오기
    }

    /**
     * multipartFile을 로컬에 File로 임시 저장함
     * @param multipartFile
     * @return
     * @throws IOException
     */
    private Optional<File> convertMultipartFileToSavedFile(MultipartFile multipartFile) throws IOException {

        File convertFile = new File(System.getProperty("user.dir") + multipartFile.getOriginalFilename()); // System.getProperty("user.dir") : 현재 프로젝트의 절대 경로를 꺼내오기
        try {
            if (convertFile.createNewFile()) { // 바로 위에서 지정한 경로에 File이 생성됨 (경로가 잘못되었다면 생성 불가능)
                try (FileOutputStream fos = new FileOutputStream(convertFile)) { // FileOutputStream 데이터를 파일에 바이트 스트림으로 저장하기 위함
                    fos.write(multipartFile.getBytes());
                    // flush가 틀렸을 수도 있음
                    // 해당 에러로 추가 -> java.lang.IllegalStateException: getOutputStream() has already been called for this response
                    fos.flush();
                }

                return Optional.of(convertFile);
            }
        } catch (IOException e) {
            log.error("convertMultipartFileToSavedFile : fail");
            throw e;
        }

        log.error("convertMultipartFileToSavedFile : fail. return empty optional");
        return Optional.empty();
    }

    /**
     * 로컬에 저장했던 임시 파일 삭제
     * @param targetFile
     */
    private void removeTempFile(File targetFile) {
        if (targetFile.delete()) {
            return;
        }
        log.warn("[FAIL] Fail to delete temp file");
//        throw new S3Exception(S3StatusCode.TEMP_FILE_DELETE_FAIL);
    }
}
