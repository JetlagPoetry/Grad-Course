% This is a sample file to overlay your quiver plot
% on the image for each image of every sequence
% Feel free to play around with this script.


for i=7:13
    X = [' Frame numbers ',num2str(i),' and ',num2str(i+1)];
    disp(X)
    
    %reusing some part of the code given in demo_optical_flow.m
    method = "LK_pyramid";
    [Vx, Vy] = demo_optical_flow('Backyard',i,i+1,method);
    
    s = size(Vx);
    step = max(s)/50;
    [X, Y] = meshgrid(1:step:s(2), s(1):-step:1);
    u = interp2(Vx, X, Y);
    v = interp2(Vy, X, Y);
 
    if(i < 10)
        
        Image_current = imread(fullfile('Backyard',strcat('frame0',num2str(i),'.png')));
    else
        Image_current = imread(fullfile('Backyard',strcat('frame',num2str(i),'.png')));
    end

    figure('visible','off');
    imagesc(unique(X),unique(Y),Image_current);
    hold all
    
    quiver(X, Y, u, v, 1, 'r', 'LineWidth', 1);
    axis image;
    
    FF=getframe;
    close;
    [Image,~]=frame2im(FF);

    imwrite(Image,strcat(method,'flow',num2str(i),num2str(i+1),'.png'));

    close;
    
end
