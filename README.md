# About
팍스스쿨 앱은 학생들이 가입한 학교에서 원하는 학습 컨텐츠를 구매하고,<br> 
해당 컨텐츠에 맞는 숙제를 교사가 지정하여 제출 받는 앱 이다

학생들은 영상 시청, 퀴즈 풀이, 플래시카드 공부, E-book 보기 등 다양한 과제를 수행하며,<br> 
교사는 학생들이 제출한 숙제를 검사하여 적절한 점수를 부여할 수 있다.

이러한 기능으로 학생들은 보다 효율적으로 학습을 진행할 수 있으며,<br> 
교사는 숙제 검사와 점수 부여를 편리하게 처리할 수 있다.<br>


# Project Architecture

## 🏗️ Architecture Overview

### Development Pattern
- **Pattern**: Model-View-Intent (**MVI**)
- **UI Framework**: Declarative UI with **Jetpack Compose**

## 📦 Package Structure

### 1. Presentation Package

#### `mvi` Package Components
1. **State**
   - Holds data used in Compose UI
   - Represents the current screen state
   - Drives UI rendering and updates

2. **Event**
   - Triggers state changes
   - Acts as a mechanism to modify the current state
   - When state changes, Compose UI automatically updates

3. **Action**
   - Notifies ViewModel of user interactions
   - Initiated from Compose UI
   - Signals ViewModel to process specific user actions

4. **ViewModel**
   - Handles business logic
   - Processes actions and updates state
   - Manages data flow and transformations

5. **Side Effect**
   - Manages additional UI interactions
   - Handles error scenarios
   - Triggers alerts or notifications to the user

### 2. `screen` Package
- Implements Compose UI screens
- Equivalent to traditional XML layouts
- Responsible for rendering user interfaces

## 🚀 Key Features
- **Reactive Programming**: Utilizing MVI architectural pattern
- **Declarative UI**: Powered by Jetpack Compose
- **Unidirectional Data Flow**: State → UI → Action → ViewModel

## 🔍 Technical Highlights
- Immutable state management
- Reactive UI updates
- Separation of concerns
- Event-driven architecture


~~~ mermaid
flowchart LR
    subgraph UI/Screen
    A[Compose UI]
    G[Activity/Lifecycle]
    end

    subgraph MVI Package
    B[Action]
    C[ViewModel]
    D[State]
    E[Event]
    F[Side Effect]
    end

    A -->|User Interaction| B
    B -->|Notify| C
    C -->|Update| D
    C -->|Trigger| E
    C -->|Generate| F
    F -->|Handle| G
    D -->|Render| A
    E -->|Modify| D
~~~  

# Sequence
~~~ mermaid
sequenceDiagram
    participant Activity as Activity
    participant Screen as Screen
    participant VM as ViewModel
    participant Service as Service/Repository

    Activity ->> Screen: 초기 State 전달
    Screen ->> VM: Action (사용자 액션)
    VM ->> Service: Request Data
    Service -->> VM: Response
    VM ->> VM: State 업데이트
    VM ->> Screen: State 전달
    Screen ->> Screen: UI 재렌더링
    
    alt 에러 발생
        VM ->> VM: Side Effect 생성
        VM ->> Activity: Side Effect 전달
        Activity ->> Activity: Side Effect 처리
    end
~~~   

# UI 
### Intro 화면
<img src="https://user-images.githubusercontent.com/10841533/235601302-695a7fca-3251-4deb-b996-3c0082f5d8eb.jpg" width="220" height="500"/>

### 선생님 화면
<div><img src="https://user-images.githubusercontent.com/10841533/235601318-cb7fa4d2-1362-4bee-803c-a5393eabee95.jpg" width="220" height="500"/>
<img src="https://user-images.githubusercontent.com/10841533/235601323-2490f0e8-7749-48ee-899a-235f8072f215.jpg" width="220" height="500"/>
<img src="https://user-images.githubusercontent.com/10841533/235601334-606ecc0b-0ec9-4df9-a8d6-bb97819fe63b.jpg" width="220" height="500"/></div>

### 학생 화면
<div><img src="https://user-images.githubusercontent.com/10841533/235601340-a465f02e-20c2-4bb9-9dc6-ec6adcee9c47.jpg" width="220" height="500"/>
<img src="https://user-images.githubusercontent.com/10841533/235601346-aadde140-c70c-4e44-9428-d142eb9460b3.jpg" width="220" height="500"/>
<img src="https://user-images.githubusercontent.com/10841533/235601353-2f1cf1b0-794c-43fa-adbe-cd6551bf59be.jpg" width="220" height="500"/></div>

# TEST
    테스트 학교 : 리틀팍스 초등학교
    테스트 아이디 : Test5
    비밀번호 : 1234
