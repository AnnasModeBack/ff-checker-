package com.annas.aifloating;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingAIService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private View boxSelectorView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isMcqMode = false;
    private int resizeStep = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(
                NotificationHelper.SERVICE_NOTIF_ID,
                NotificationHelper.buildServiceNotification(this)
        );

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_floating, null);

        final int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 36;
        params.y = 220;

        windowManager.addView(floatingView, params);
        Animation3DHelper.apply3DTiltEntrance(floatingView, 0L);

        final LinearLayout bubbleContainer = floatingView.findViewById(R.id.bubbleContainer);
        ImageView ivBubbleIcon = floatingView.findViewById(R.id.ivBubbleIcon);
        ImageView ivPanelLogo = floatingView.findViewById(R.id.ivPanelLogo);
        final LinearLayout panelContainer = floatingView.findViewById(R.id.panelContainer);
        final ImageButton btnMinimize = floatingView.findViewById(R.id.btnMinimize);
        final Button btnToggleBoxSelector = floatingView.findViewById(R.id.btnToggleBoxSelector);
        Button btnPaste = floatingView.findViewById(R.id.btnPasteClipboard);
        final Button btnModeMcq = floatingView.findViewById(R.id.btnModeMCQ);
        ImageButton btnClearPrompt = floatingView.findViewById(R.id.btnClearPrompt);
        final EditText etPrompt = floatingView.findViewById(R.id.etPrompt);
        final Button btnSolve = floatingView.findViewById(R.id.btnSolve);
        final TextView tvResult = floatingView.findViewById(R.id.tvResult);
        Button btnCopy = floatingView.findViewById(R.id.btnCopyResult);

        Animation3DHelper.start3DFloatingIconLoop(ivBubbleIcon);
        Animation3DHelper.start3DFloatingIconLoop(ivPanelLogo);

        bubbleContainer.setOnTouchListener(new View.OnTouchListener() {
            private int startX = 0;
            private int startY = 0;
            private float touchX = 0f;
            private float touchY = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = startX + (int) (event.getRawX() - touchX);
                        params.y = startY + (int) (event.getRawY() - touchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        float dx = Math.abs(event.getRawX() - touchX);
                        float dy = Math.abs(event.getRawY() - touchY);
                        if (dx < 12 && dy < 12) {
                            Animation3DHelper.pop3DClick(bubbleContainer);
                            bubbleContainer.setVisibility(View.GONE);
                            panelContainer.setVisibility(View.VISIBLE);
                            Animation3DHelper.apply3DTiltEntrance(panelContainer, 0L);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });

        btnMinimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnMinimize);
                panelContainer.setVisibility(View.GONE);
                bubbleContainer.setVisibility(View.VISIBLE);
                Animation3DHelper.apply3DTiltEntrance(bubbleContainer, 0L);
            }
        });

        btnToggleBoxSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnToggleBoxSelector);
                showBoxSelector(overlayType, etPrompt, tvResult, btnSolve, bubbleContainer, panelContainer);
            }
        });

        btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(v);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String clipText = "";
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    ClipData clipData = clipboard.getPrimaryClip();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        CharSequence cs = clipData.getItemAt(0).coerceToText(FloatingAIService.this);
                        if (cs != null) {
                            clipText = cs.toString();
                        }
                    }
                }
                if (!clipText.trim().isEmpty()) {
                    etPrompt.setText(clipText);
                    Toast.makeText(FloatingAIService.this, "Soal ditempel dari clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FloatingAIService.this, "Clipboard kosong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnModeMcq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnModeMcq);
                isMcqMode = !isMcqMode;
                btnModeMcq.setText(isMcqMode ? "Mode PG: ON" : "Mode PG");
                Toast.makeText(
                        FloatingAIService.this,
                        isMcqMode ? "Mode Pilihan Ganda Aktif" : "Mode Auto Tugas Aktif",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnClearPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(v);
                etPrompt.setText("");
                Toast.makeText(FloatingAIService.this, "Input soal dibersihkan", Toast.LENGTH_SHORT).show();
            }
        });

        btnSolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnSolve);
                String promptText = etPrompt.getText().toString().trim();
                if (promptText.isEmpty()) {
                    Toast.makeText(FloatingAIService.this, "Masukkan soal atau pakai Kotak Centang SVG dulu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendPromptToGoogleAI(promptText, tvResult, btnSolve);
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(v);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Jawaban Google AI", tvResult.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(FloatingAIService.this, "Jawaban disalin!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showBoxSelector(
            int overlayType,
            final EditText etPrompt,
            final TextView tvResult,
            final Button btnSolve,
            final LinearLayout bubbleContainer,
            final LinearLayout panelContainer
    ) {
        removeBoxSelector();

        final View boxView = LayoutInflater.from(this).inflate(R.layout.overlay_box_selector, null);
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = 80;
        layoutParams.y = 320;

        boxSelectorView = boxView;
        windowManager.addView(boxView, layoutParams);
        Animation3DHelper.apply3DTiltEntrance(boxView, 0L);

        final LinearLayout boxFrame = boxView.findViewById(R.id.boxFrame);
        TextView dragHandle = boxView.findViewById(R.id.tvBoxDragHandle);
        final ImageButton btnConfirmCheck = boxView.findViewById(R.id.btnConfirmCheckAI);
        ImageButton btnClose = boxView.findViewById(R.id.btnCloseBox);
        final Button btnResize = boxView.findViewById(R.id.btnResizeBox);

        dragHandle.setOnTouchListener(new View.OnTouchListener() {
            private int initX = 0;
            private int initY = 0;
            private float downX = 0f;
            private float downY = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = layoutParams.x;
                        initY = layoutParams.y;
                        downX = event.getRawX();
                        downY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        layoutParams.x = initX + (int) (event.getRawX() - downX);
                        layoutParams.y = initY + (int) (event.getRawY() - downY);
                        windowManager.updateViewLayout(boxView, layoutParams);
                        return true;
                    default:
                        return false;
                }
            }
        });

        btnResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resizeStep = (resizeStep + 1) % 3;
                int wDp = 260;
                int hDp = 150;
                if (resizeStep == 1) {
                    wDp = 310;
                    hDp = 195;
                } else if (resizeStep == 2) {
                    wDp = 345;
                    hDp = 240;
                }
                float density = getResources().getDisplayMetrics().density;
                ViewGroup.LayoutParams lp = boxFrame.getLayoutParams();
                lp.width = (int) (wDp * density);
                lp.height = (int) (hDp * density);
                boxFrame.setLayoutParams(lp);
                Animation3DHelper.pop3DClick(btnResize);
            }
        });

        btnConfirmCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation3DHelper.pop3DClick(btnConfirmCheck);
                String targetPrompt = BoxTargetHelper.resolveTargetContent(
                        FloatingAIService.this,
                        layoutParams.x,
                        layoutParams.y,
                        boxFrame.getWidth(),
                        boxFrame.getHeight()
                );
                etPrompt.setText(targetPrompt);
                removeBoxSelector();

                bubbleContainer.setVisibility(View.GONE);
                panelContainer.setVisibility(View.VISIBLE);
                Animation3DHelper.apply3DTiltEntrance(panelContainer, 0L);

                sendPromptToGoogleAI(targetPrompt, tvResult, btnSolve);
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBoxSelector();
            }
        });
    }

    private void sendPromptToGoogleAI(final String promptText, final TextView tvResult, final Button btnSolve) {
        SharedPreferences prefs = getSharedPreferences("annas_ai_3d_java_prefs", Context.MODE_PRIVATE);
        String apiKey = prefs.getString("api_key", "");
        String savedMode = prefs.getString("mode_name", "Auto Tugas & Jawaban Lengkap");
        final String activeMode = isMcqMode ? "Pilihan Ganda Cepat" : savedMode;

        tvResult.setText("Mengirim ke Google AI & memproses jawaban...");
        btnSolve.setEnabled(false);

        GeminiClient.solveTaskAsync(apiKey, activeMode, promptText, new GeminiClient.AnswerCallback() {
            @Override
            public void onResult(final String answer) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText(answer);
                        btnSolve.setEnabled(true);
                        NotificationHelper.showAnswerNotification(
                                FloatingAIService.this,
                                "Jawaban Google AI Siap",
                                answer
                        );
                    }
                });
            }
        });
    }

    private void removeBoxSelector() {
        if (boxSelectorView != null) {
            try {
                windowManager.removeView(boxSelectorView);
            } catch (Exception ignored) {
            }
            boxSelectorView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeBoxSelector();
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception ignored) {
            }
        }
    }
}
