readPositions

numPositions = size(XYZ,1);

close all;

Iname = 'c1.jpg';
I = imread(Iname);

[P, K, R, C] = calibrate(XYZ, xy1);
K1 = [50*size(I,2)/23.6,0,0;0,50*size(I,1)/15.8,0;0,0,1];
K2 = [25*size(I,2)/2/23.6,0,178;0,25*size(I,1)/15.8/2,268;0,0,1];
theta = 0;
% Rotation = [cos(theta/180*pi),0,sin(theta/180*pi);0,1,0;-sin(theta/180*pi),0,cos(theta/180*pi)];
Rotation = [1,0,0;0,cos(theta/180*pi),sin(theta/180*pi);0,-sin(theta/180*pi),cos(theta/180*pi)];
M = K2*Rotation*inv(K1);

I2 = zeros(size(I,1)/2,size(I,2)/2,3);
for i = 1:size(I,1)
    for j = 1:size(I,2)
        temp = M*[i;j;1];
        temp = temp./temp(3,1);
        if temp(1,1)<1
            temp(1,1) = 1;
        elseif temp(1,1)>size(I2,1)
            temp(1,1) = size(I2,1);
        end
        if temp(2,1)<1
            temp(2,1) = 1;
        elseif temp(2,1)>size(I2,2)
            temp(2,1) = size(I2,2);
        end
        I2(round(temp(1,1)),round(temp(2,1)),1) = I(i,j,1);
        I2(round(temp(1,1)),round(temp(2,1)),2) = I(i,j,2);
        I2(round(temp(1,1)),round(temp(2,1)),3) = I(i,j,3);
    end
end
imshow(uint8(I2));