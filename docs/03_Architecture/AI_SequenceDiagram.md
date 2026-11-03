sequenceDiagram
autonumber
actor Admin as Quản trị viên
participant Ctrl as NarrationController
participant Svc as NarrationService
participant TaskRepo as AiTaskRepository
participant TransRepo as TranslationRepository
participant AI_Trans as TranslationProvider<br/>(Interface)
participant AI_TTS as TextToSpeechProvider<br/>(Interface)

    %% LUỒNG ĐỒNG BỘ: NHẬN REQUEST & TRẢ VỀ NHANH
    rect rgb(230, 245, 255)
        Note over Admin, TaskRepo: 1. LUỒNG ĐỒNG BỘ (SYNC) - Phản hồi ngay lập tức
        Admin->>Ctrl: POST /api/narrations<br/>(Original Text, SiteId)
        activate Ctrl

        Ctrl->>Svc: createNarrationAndTriggerAI()
        activate Svc

        Svc->>Svc: Tính mã băm MD5 (ContentHash)
        Svc->>Svc: Lưu Narration gốc vào DB

        Svc->>TaskRepo: INSERT AiTranslationTasks<br/>(Status: PENDING)
        TaskRepo-->>Svc: TaskId

        Svc-->>Ctrl: Trả về NarrationDTO
        deactivate Svc

        Ctrl-->>Admin: HTTP 202 Accepted<br/>(Kèm TaskId để theo dõi)
        deactivate Ctrl
    end

    %% LUỒNG BẤT ĐỒNG BỘ: XỬ LÝ AI NGẦM
    rect rgb(255, 245, 230)
        Note over Svc, AI_TTS: 2. LUỒNG BẤT ĐỒNG BỘ (ASYNC) - Chạy ngầm (@Async Worker)
        activate Svc
        Svc->>TaskRepo: UPDATE Task<br/>(Status: PROCESSING)

        alt Cache Hit (Hash đã tồn tại)
            Svc->>Svc: Nội dung không đổi.<br/>Bỏ qua gọi API, Clone bản dịch cũ.
            Svc->>TransRepo: Liên kết AudioUrl cũ với Narration mới
        else Cache Miss (Nội dung mới cần dịch)

            loop Cho mỗi Ngôn ngữ đang Active
                %% Bước Dịch thuật
                Svc->>AI_Trans: translate(text, targetLang)
                activate AI_Trans
                Note right of AI_Trans: Adapter gọi Google/Azure<br/>hoặc Python Mock
                AI_Trans-->>Svc: translated_text
                deactivate AI_Trans

                %% Bước Tổng hợp giọng nói (TTS)
                Svc->>AI_TTS: generateAudio(translated_text, targetLang)
                activate AI_TTS
                Note right of AI_TTS: Lưu file mp3 xuống đĩa cứng<br/>hoặc Cloud Storage
                AI_TTS-->>Svc: audioUrl (/static/audio/...)
                deactivate AI_TTS

                %% Lưu kết quả
                Svc->>TransRepo: INSERT Translations<br/>(LanguageId, text, audioUrl)
            end
        end

        %% Xử lý kết quả Task
        alt Nếu toàn bộ thành công
            Svc->>TaskRepo: UPDATE Task<br/>(Status: COMPLETED)
        else Nếu có Exception (Rớt mạng, Hết Quota)
            Svc->>TaskRepo: UPDATE Task<br/>(Status: FAILED, ErrorMessage)
        end
        deactivate Svc
    end
