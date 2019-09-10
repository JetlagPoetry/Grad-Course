function [Vx,Vy] = demo_optical_flow(folder_name,frame_number_1,frame_number_2,type_LK)

% -------------------------------- %
% DO NOT CHANGE THIS FUNCTION !!!! %
% -------------------------------- %

% This is just a helper function to test your code
% This is a demo to show the optical flow in quiver plot
% Example of using it:
% demo_optical_flow('Backyard',10,11, "LK_iterative")


if(nargin == 0)
    folder_name = 'Backyard';
    frame_number_1 = 7;
    frame_number_2 = frame_number_1 + 1;
elseif(nargin == 1)
    frame_number_1 = 7;
    frame_number_2 = frame_number_1 + 1;
elseif(nargin ==2)
    frame_number_2 = frame_number_1 + 1;
end

addpath(folder_name);

frame_1 = read_image(folder_name,frame_number_1);
frame_2 = read_image(folder_name,frame_number_2);



[Vx,Vy] = compute_LK_optical_flow(frame_1,frame_2,type_LK);


plotflow(Vx,Vy);



end
function plotflow(Vx,Vy)

% -------------------------------- %
% DO NOT CHANGE THIS FUNCTION !!!! %
% -------------------------------- %

s = size(Vx);
step = max(s)/50;
[X, Y] = meshgrid(1:step:s(2), s(1):-step:1);
u = interp2(Vx, X, Y);
v = interp2(Vy, X, Y);
quiver(X, Y, u, v,1, 'k', 'LineWidth', 1);
axis image;


end
function I = read_image(folder_name,index)

% -------------------------------- %
% DO NOT CHANGE THIS FUNCTION !!!! %
% -------------------------------- %

if(index < 10)
    I = imread(fullfile(folder_name,strcat('frame0',num2str(index),'.png')));
else
    I = imread(fullfile(folder_name,strcat('frame',num2str(index),'.png')));
end

end
