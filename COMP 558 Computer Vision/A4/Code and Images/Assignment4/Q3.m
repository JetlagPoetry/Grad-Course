%Code from Q1_Tester.m
clear
close all
XYZ = [0 0 0; ...
    0 0 1;
    0 1 0;
    0 1 1;
    1 0 0;
    1 0 1;
    1 1 0;
    1 1 1];
K = [300 0 20; 0 300 -30; 0 0 1];
num_degrees = 15;
theta_y = num_degrees *pi/180;
R_y = [cos(theta_y) 0 sin(theta_y);  0 1 0;  -sin(theta_y) 0  cos(theta_y)];
C = [0.5 0.5 -2]';
P = K * R_y * [ eye(3), -C];
close all
figure; hold on;
numPositions = size(XYZ,1);

xy = zeros(numPositions, 2);
for j = 1:numPositions
    p = P*[ XYZ(j,1) XYZ(j,2) XYZ(j,3)  1]';
    x = p(1)/p(3);
    y = p(2)/p(3);
    
    xy(j,1) = x;
    xy(j,2) = y;
    plot(x, y,'sk');
end

[P, K, R, C] = calibrate(XYZ, xy);
M1 = [1,0,10;0,1,0;0,0,1];%shifting K
K_new = M1*K;
M2 = inv(K)*M1*K;%shifting R
R_new = M2*R;
M3 = inv(R)*inv(K)*M1*K*R;%shifting C
I_C_new = M3*[eye(3),-C];
P1 = K_new * R * [eye(3),-C];
P2 = K * R_new * [eye(3),-C];
P3 = K * R * I_C_new;

M4 =  [1.2,0,0;0,1.2,0;0,0,1];
K_new = M4*K;%expanding K
M5 = inv(R)*inv(K)*M4*K*R;
I_C_new = M5*[eye(3),-C]; %expanding C
P4 = K_new * R * [eye(3),-C];
P5 = K * R * I_C_new;

xy = zeros(numPositions, 2);
for j = 1:numPositions
    p = P4*[ XYZ(j,1) XYZ(j,2) XYZ(j,3)  1]';
    x = p(1)/p(3);
    y = p(2)/p(3);
    %  Draw the points in black stars according to the fit least squares model.
    plot(x,y,'*k','color','r');
end

for j = 1:numPositions
    p = P5*[ XYZ(j,1) XYZ(j,2) XYZ(j,3)  1]';
    x = p(1)/p(3);
    y = p(2)/p(3);
    %  Draw the points in black stars according to the fit least squares model.
    plot(x,y,'sk','color','g');
end

