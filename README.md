# README
대규모 다중 화상 채팅 서비스 환경을 위한, WebRTC(SFU) 통신 방식을 구현한  프로젝트입니다.

## Architecture Structure
![image](https://github.com/howudong/Face-Out/assets/53307093/b7ee9645-9ed6-4304-988f-cb04fe70d781)

## 각 기술 스택 설명 및 선정 이유

- `SFU방식을 선택한 이유`
    - 미디어 서버를 통해, endpoint간의 미디어 스트림을 중계하는 방식
    - 순차적으로 들어오는 사용자 즉, 이용자가 증가하면 증가할수록 피어가 많아지기 때문에 다자간 커뮤니케이션을 지원하기 위해 미디어 서버를 활용
    - 다른 방식인 시그널링의 경우 6~8이상부터는 순차적 참가시, 점점 느려지는 현상을 대체할 수 있는 프로젝트에 최적화된 방식
    - 중앙 서버가 모든 데이터를 중계하고 선택적으로 전송하므로, 서버 측에서 대역폭을 효과적으로 관리하고 필요에 따라 확장이 가능

- `Kurento Media Server`
    - 플러그인 아키텍처를 지원하여 사용자가 필요에 맞게 기능을 확장 가능
    - 프로젝트 특성상, 상대적으로 비율이 많았던, java코드로 인해, java기반으로 설계된  편리성으로 채택
    - WebRTC 기술을 커스터마이징하기 좋은 라이브러리임
    - Kurento 기반의 Openvidu 라이브러리를 사용하지 않은 이유?
        - Openvidu는 화상 채팅의 측면과 관련된 제한적인 api만을 사용 가능
        → WebRTC 기술을 커스터마이징해서 사용하는데 유연성이 떨어지기 때문에 사용하지 않음


- `React(JS)`
    - 공식 코드는 jQuery와 JavaScript로만 작성되어 있었지만, 팀 협업의 필요성과 함께 React를 도입
    - 해당 레포지토리는 SFU의 통신방식을 구현했지만, 추후 다음과 같은 사항의 확장성을 고려
        - 2D 캐릭터가 움직이는 환경에서, 특정 바운더리에 진입하면 마이크 및 카메라 권한을 요청하는 알림
        - 사용자가 수락하면 즉시 React 페이지에 모달 형식으로 화상 채팅이 시작
        - 캐릭터이동 x, y좌표 그리고 채팅또한 webrtc로 진행하기 때문에, 커스터마이징이 필요
    - 따라서, 이를 최소한의 component단위로 동작하도록 jsx파일에 녹여 만들 필요성을 느낌


- `Docker`
    - Docker를 사용함으로써 아래와 같은 이점을 얻을 수 있었음
        - Nginx 서버와 Kurento 미디어 서버, Application Sever(Backend Server)를 하나의 AWS EC2에 띄울 수 있었음 
          → 서버 비용 절감 및 EC2 및 공간의 효율적인 사용
        - Docker-Compose 기능을 사용함으로써, 각각의 서버를 한 곳에서 효율적으로 유지 보수


- `Nginx`
    - NAT 환경에서 비디오 권한을 얻기 위해선 SSL 인증이 필수
    → 클라이언트와 서버의 ssl 인증을 nginx로 통합적으로 해결
    - 네트워크 부하에 대한 로드 밸런싱 진행


- `SpringBoot`
    - Kurento Media Server 라이브러리와 이에 관한 Docs를 적극적으로 제공
    - Kurento 미디어 처리 로직에 중점을 둘 수 있음
        - Spring의 IoC(Inversion of Control)와 DI(Dependency Injection) 기능을 활용해 Kurento 미들웨어와 애플리케이션 로직을 결합
    - 스프링 부트의 자동 설정과 서버 기능(톰캣 등)을 활용하여 배포와 운영을 간편하게 처리


- `Github-Action`
    - AWS EC2 서버에 지속적인 배포를 위한 기술로써 사용(CI/CD)
    - jenkins 보다 빠른 속도와 쉬운 조작을 제공
