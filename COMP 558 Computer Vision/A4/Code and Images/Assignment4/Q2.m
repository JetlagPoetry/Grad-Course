
% close all
readPositions  %  reads given positions of XYZ and xy1 and xy2 for two images

numPositions = size(XYZ,1);

% close all
Iname = 'c1.jpg';
xy = xy1;
I = imread(Iname);
NX = size(I,2);
NY = size(I,1);
imageInfo = imfinfo(Iname);
figure;
imshow(I);
title(Iname);
hold on

%  Draw in green the keypoints locations that were hand selected.

for j = 1:numPositions
    plot(xy(j,1),xy(j,2),'g*');
end

%Calculate camera calibrate coordinate
[P, K, R, C] = calibrate(XYZ, xy);

q = 2;
%Objective A
if q==1
    %Perform a translation to K matrix
    M1 = [1,0,200;0,1,0;0,0,1];
    K_new = M1*K;
    %Change R matrix to get similiar result
    M2 = [1,0,0.033;0,1,0;0,0,1];
    R_new = M2*R;
    %Change C vector to get similiar result
    M3 = [1,0,0,-33;0,1,0,3;0,0,1,68;0,0,0,1];
    C_new = M3*[C;1];
    C_new = C_new./C_new(4,1);
    C_new = C_new(1:3,:);

    P1 = K_new * R * [eye(3), -C];
    P2 = K * R_new * [eye(3), -C];
    P3 = K * R * [eye(3), -C_new];
    
    % ???
    %Please change the statement below to perform different P matrices
    P = P2;
    % P = P2;
    % P = P3;
else
    % Objective B
    M1 = [1.2,0,0;0,1.2,0;0,0,1];
    K_new = M1*K;
    M2 = [1.05,-0.07,-0.071;-0.04,1.18,-0.022;-0.06,-0.03,1.17];
    I_C = M2*[eye(3),-C];

    P1 = K_new * R * [eye(3), -C];
    P2 = K * R * I_C;
    
    %Change the statemenet below
%     P = P1;
    P = P2;
end

%  Normalize the K so that the coefficients are more meaningful.
K = K/K(3,3);
for j = 1:numPositions
    p = P*[ XYZ(j,1) XYZ(j,2) XYZ(j,3)  1]';
    x = p(1)/p(3);
    y = p(2)/p(3);

    %  Draw in white square the projected point positions according to the fit model.

    plot(ceil(x),ceil(y),'ws');
end

