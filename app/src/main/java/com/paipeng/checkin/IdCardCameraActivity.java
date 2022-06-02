package com.paipeng.checkin;

import com.paipeng.checkin.ui.IdCardRectView;

public class IdCardCameraActivity extends FaceCameraActivity{

    private IdCardRectView idCardRectView;
    @Override
    protected void initView() {
        super.initView();
        rectView = findViewById(R.id.single_camera_frame_rect_view);
        idCardRectView = findViewById(R.id.single_camera_idcard_rect_view);
    }
}
