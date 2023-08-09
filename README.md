# ChatApp
Android Studio &amp; FireBase를 활용한 ChatApp
# 파이어베이스를 활용한 안드로이드 채팅앱

# UI 구성
## 로그인 화면

로그인과 회원가입 버튼이 있다.

![image](https://github.com/jjw6712/ChatApp/assets/91660287/eddfbd50-3325-47ac-b888-db9d8b5d9cc5)


## 회원가입 화면

회원가입은 email로 진행하면 된다.

![image](https://github.com/jjw6712/ChatApp/assets/91660287/026653ed-be5c-4c1a-80d6-52ff2a207b09)


## 친구 목록 화면

상단에 로그인한 사람의 아이템이 표시되고, 하단에 친구들 과의 구분선을 둠
구분선 밑에는 현재 친구의 수를 보여준다.
![image](https://github.com/jjw6712/ChatApp/assets/91660287/b4aa9e65-0f7e-4252-b779-9ef5c18d2678)


## 친구 아이템 클릭 시 

친구가 설정한 프로필 사진과 배경사진을 보여주며, 1:1 채팅 시작하기 버튼을 누르면 해당 친구와의 채팅방을 호출한다.
![image](https://github.com/jjw6712/ChatApp/assets/91660287/349793ae-3ae1-488d-959f-b64fd374e25a)
![image](https://github.com/jjw6712/ChatApp/assets/91660287/d5ab9559-72c5-4cf5-bc35-532b54502c2c)


## 로그인한 유저 아이템 클릭 시

로그인한 자신의 아이템을 클릭하면 즉시 프로필 설정 화면을 호출하여 프로필 사진과 배경화면을 눌러 갤러리에서 사진을 선택해 바꿀 수 있다.
![image](https://github.com/jjw6712/ChatApp/assets/91660287/cb045e25-d78c-4010-995b-015a1ff3e57b)


## 초기 프로필 설정 시

프로필 사진과 배경 사진은 각각의 이미지 뷰를 클릭하면 갤러리로 이동하여 사진을 선택해 설정할 수 있고, 
"눌러서 사진 가져오기" 텍스트는 사진이 적용되면 비활성화 한다.

배경화면을 설정하지 않았을 시 기본 백그라운드 컬러로 설정된다.
![image](https://github.com/jjw6712/ChatApp/assets/91660287/04e0f0c9-e186-4a73-ad83-48b5a7f3e128)
![image](https://github.com/jjw6712/ChatApp/assets/91660287/94217780-037f-4e12-b382-c64aa46cb585)


## 채팅 목록 화면

현재 로그인 된 사용자와 생성된 채팅방의 목록을 가져와서 보여준다.
상대방의 이름, 사진, 마지막으로 송 수신한 메세지와 그 시간을 보여준다.
채팅이 업데이트 될 때마다 실시간 데이터베이스에서 받아와 업데이트 한다.
 ![image](https://github.com/jjw6712/ChatApp/assets/91660287/2425f88a-af14-43b5-93b8-5299d2eba11c)


## 채팅 화면

상대방의 사진, 이름을 보여주고 메세지와 메세지를 송 수신한 시간을 함께 보여준다.
![image](https://github.com/jjw6712/ChatApp/assets/91660287/72ac3fdc-d387-41b5-872c-2a95289458ea)![image](https://github.com/jjw6712/ChatApp/assets/91660287/36274639-3efe-4473-a104-9c661fe2be48){: width="50%" height="50%"}




# Android Studio 디렉토리 구조
    
![image](https://github.com/jjw6712/ChatApp/assets/91660287/25fff801-c9ec-4f93-9128-089df67cf7af)![image](https://github.com/jjw6712/ChatApp/assets/91660287/aa8cecc9-06dc-4783-8636-0cc70d090991)![image](https://github.com/jjw6712/ChatApp/assets/91660287/fea11282-3146-43b7-8cbf-2aa7b11e97bd)



 

# Firebase의 Authentication를 활용하여 회원가입, 로그인 기능을 구현 

![image](https://github.com/jjw6712/ChatApp/assets/91660287/c44bb7ec-ddfc-428b-8629-e50a6346827d)


# Firebase의 Realtime Datebase를 활용하여 채팅 기능 구현

![image](https://github.com/jjw6712/ChatApp/assets/91660287/2a5af66c-33c6-4d54-9343-8a5886238426)




# Firebase의 Storage를 활용하여 유저의 프로필 사진과 배경사진의 저장소로 사용

![image](https://github.com/jjw6712/ChatApp/assets/91660287/4c3cb963-b266-4dbd-909c-eaba2a202d37) ![image](https://github.com/jjw6712/ChatApp/assets/91660287/60ba26ed-7833-45ed-bcdf-f2d003016a12)



