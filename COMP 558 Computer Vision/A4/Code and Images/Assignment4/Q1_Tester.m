%  Q1_Tester - This tester code should help you to check if your calibrate.m is working correctly.
%   
%  It creates a projection matrix with known K, R, C matrices,  and it takes a cube object
%  and projects it into an image using the projection matrix.
%  Then it uses the object and projected points as inputs to your calibrate.m   
%  If your calibrate function is working correctly,  you should get back
%  the K, R, C matrices that you started with.

clear
close all

%  The scene object is a unit cube.

XYZ = [0 0 0; ...
    0 0 1;
    0 1 0;
    0 1 1;
    1 0 0;
    1 0 1;
    1 1 0;
    1 1 1];

%  Set up a projection matrix by choosing K, R, C.

%  For K, just go with coordinates on the projection plane, and put the projection plane at f = 10.
%  Set the principal point to be slightly different from (0,0),  namely (20, -30).  
%
%  Note that in examples with real images, we would expect the principal point to be roughly
%  at the center pixel (Nx/2, Ny/2),  since the (1,1) and (Nx,Ny) would be at opposite corners of the sensor.
%  We would also expect the K11 and K22 coefficients to include a change in
%  units from mm to top pixels.   We have not done this here.   Essentially
%  we are treating 1 mm = 1 pixel.

K = [300 0 20; 0 300 -30; 0 0 1];

%  Set camera axes to be near parallel to the object (world) axes. 
%  Only have a small rotation about y axis.

num_degrees = 15;
theta_y = num_degrees *pi/180;
R_y = [cos(theta_y) 0 sin(theta_y);  0 1 0;  -sin(theta_y) 0  cos(theta_y)];

%  When theta_y = 0,  the camera coordinate axes are same as object axes.
%  We put the camera at a negative z value.  It is looking in the positive z
%  direction.   Note the XY of camera corresponds to the center XY of the
%  cube.

C = [0.5 0.5 -2]';

P = K * R_y * [ eye(3), -C];

%  now plot the projection of the cube corners for this chosen projection matrix

close all
figure; hold on;

numPositions = size(XYZ,1);

%  Draw in black square the projected point positions according to the true model.

xy = zeros(numPositions, 2);
for j = 1:numPositions
    p = P*[ XYZ(j,1) XYZ(j,2) XYZ(j,3)  1]';
    x = p(1)/p(3);
    y = p(2)/p(3);
    
    xy(j,1) = x;
    xy(j,2) = y;
    plot(x, y,'sk');
end

%%
%  Now, given only the model points XYZ and the image points xy,  
%  we solve for K, R, and C using calibrate.m and check if we get the correct answer.  

%  add a bit of noise to the xy positions to see how stable it is
%  xy = xy + .01*randn(size(xy));

[P_est, K_est, R_est, C_est] = calibrate(XYZ, xy);

%  Normalize so that K_est(3,3) is 1..
K_est = K_est/K_est(3,3);

for j = 1:numPositions
    p = P_est*[ XYZ(j,1) XYZ(j,2) XYZ(j,3)  1]';
    x = p(1)/p(3);
    y = p(2)/p(3);
    %  Draw the points in black stars according to the fit least squares model.
    plot(x,y,'*k');
end

disp("Here are the K and K_est (side by side)");
disp([K, K_est] )
disp("Here are the C and C_est (side by side)");
disp( [C, C_est] )
disp("Here are the R_y and R_est (side by side)");
disp( [R_y, R_est] )


